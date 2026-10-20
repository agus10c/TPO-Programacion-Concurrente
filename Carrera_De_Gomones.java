import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Carrera_De_Gomones implements Actividad {

    private static final int CANT_BICICLETAS = 10;
    private static final int CAPACIDAD_TREN = 15;
    private static final int CANT_GOMONES_INDIVIDUALES = 8;
    private static final int CANT_GOMONES_DOBLES = 5;
    private static final int GOMONES_PARA_LARGADA = 6;

    private final Tiempo tiempo;
    private final Random random = new Random();
    private final Lock lockCarrera = new ReentrantLock(true);
    private final Lock lockBolsos = new ReentrantLock(true);
    private final Condition hayVisitantes = lockCarrera.newCondition();
    private final Condition largadaLista = lockCarrera.newCondition();
    private final Condition hayBolsosPendientes = lockBolsos.newCondition();
    private final Condition hayBolsosParaRetrirar = lockBolsos.newCondition();
    private final Semaphore bicicletas;
    private final Semaphore tren;
    private final Semaphore gomonesIndividuales;
    private final Semaphore gomonesDobles;

    private final List<Integer> bolsosPendientes = new ArrayList<>();

    private final Map<Integer, Boolean> bolsosParaRetirar = new HashMap<>();
    private int gomonesListos;
    private int cantVisitantesEnGomonesDobles = 0;
    private boolean largadaHabilitada = false;

    public Carrera_De_Gomones(Tiempo tiempo) {
        this.tiempo = tiempo;
        this.bicicletas = new Semaphore(CANT_BICICLETAS, true);
        this.tren = new Semaphore(CAPACIDAD_TREN, true);
        this.gomonesIndividuales = new Semaphore(CANT_GOMONES_INDIVIDUALES, true);
        this.gomonesDobles = new Semaphore(CANT_GOMONES_DOBLES * 2, true);
    }

    private void viajarEnBicicleta(Visitante v) throws InterruptedException {
        bicicletas.acquire();
        try {
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " toma una bicicleta.");
            tiempo.esperarMinutos(5 + random.nextInt(5));
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " llega al inicio en bicicleta.");
        } finally {
            bicicletas.release();
        }
    }

    private void viajarEnTren(Visitante v) throws InterruptedException {
        tren.acquire();
        try {
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " sube al tren.");
            tiempo.esperarMinutos(5 + random.nextInt(5));
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " llega al inicio en tren.");
        } finally {
            tren.release();
        }
    }

    private int guardarBolso(Visitante v) {
        int nroBolso = v.getId();

        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " guarda su bolso N°" + nroBolso + ".");

        lockBolsos.lock();
        bolsosPendientes.add(nroBolso);
        hayBolsosPendientes.signalAll();
        lockBolsos.unlock();
        return nroBolso;
    }

    public List<Integer> retirarBolsosPendientes() throws InterruptedException { // La camioneta busca los bolsos para
                                                                                 // llevar
        lockBolsos.lock();
        while (bolsosPendientes.isEmpty()) {
            if (!tiempo.estaActivo())
                return null;
            hayBolsosPendientes.await();
        }
        List<Integer> copia = new ArrayList<>(bolsosPendientes);
        bolsosPendientes.clear();
        lockBolsos.unlock();
        return copia;

    }

    public void depositarBolso(int nroBolso) {
        lockBolsos.lock();
        bolsosParaRetirar.put(nroBolso, true);
        hayBolsosParaRetrirar.signalAll(); // avisa a los visitantes que esperan
        lockBolsos.unlock();
    }

    private void retirarBolso(Visitante v, int nroBolso) throws InterruptedException { // Los visitantes retiran los
                                                                                       // bolsos
        lockBolsos.lock();
        while (!bolsosParaRetirar.containsKey(nroBolso)) {
            if (!tiempo.estaActivo()) {
                System.out.println("[" + tiempo.imprimirTiempo() + "] "
                        + v + " El parque cerro. Le devolvieron su bolso.");
                return;
            }
            hayBolsosParaRetrirar.await();
        }
        bolsosParaRetirar.remove(nroBolso);
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " retira su bolso N°" + nroBolso + ".");
        lockBolsos.unlock();
    }

    public int cantidadBolsosPendientes() {

        lockBolsos.lock();
        int size = bolsosPendientes.size();
        lockBolsos.unlock();
        return size;
    }

    private void incrementarGomonesListos(boolean esDoble) {
        lockCarrera.lock();
        if (esDoble) {
            if (cantVisitantesEnGomonesDobles % 2 == 0) {
                gomonesListos++;
            }
        } else {
            gomonesListos++;
        }
        lockCarrera.unlock();
    }

    private void decrementarGomonesListos(boolean esDoble) {
        lockCarrera.lock();
        if (esDoble) {
            if (cantVisitantesEnGomonesDobles % 2 == 0) {
                gomonesListos--;
            }
        } else {
            gomonesListos--;
        }
        lockCarrera.unlock();
    }

    private boolean esperarLargada() throws InterruptedException {
        lockCarrera.lock();
        try {
            while (!largadaHabilitada && tiempo.actividadesAbiertas()) {
                largadaLista.await();
            }
            gomonesListos--;
            if (gomonesListos == 1) {
                largadaHabilitada = false;
                gomonesListos = 0;
            }
            if (!tiempo.actividadesAbiertas()) {
                return false;
            }
            return true;
        } finally {
            lockCarrera.unlock();
        }
    }

    public void habilitarCarrera() {
        lockCarrera.lock();
        if (gomonesListos >= GOMONES_PARA_LARGADA) {
            largadaHabilitada = true;
            largadaLista.signalAll();
        }
        lockCarrera.unlock();
    }

    public void notificarCierre() {
        lockCarrera.lock();
        largadaLista.signalAll();
        hayVisitantes.signalAll();
        lockCarrera.unlock();
        lockBolsos.lock();
        hayBolsosParaRetrirar.signalAll();
        hayBolsosPendientes.signalAll();
        lockBolsos.unlock();
    }

    @Override
    public void realizarActividad(Visitante v) throws InterruptedException {
        if (!tiempo.actividadesAbiertas()) {
            return;
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " Va a hacer Carrera de gomones");
        if (random.nextBoolean()) {
            viajarEnBicicleta(v);
        } else {
            viajarEnTren(v);
        }

        int nroBolso = guardarBolso(v);
        Semaphore gomon;
        boolean esDoble = random.nextBoolean();
        String tipo;
        if (esDoble) {
            gomon = gomonesDobles;
            tipo = "doble";
        } else {
            gomon = gomonesIndividuales;
            tipo = "individual";
        }
        gomon.acquire();
        if (esDoble)
            cantVisitantesEnGomonesDobles++;
        try {
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " toma un gomón " + tipo + ".");
            incrementarGomonesListos(esDoble);
            boolean seLargo = esperarLargada();
            decrementarGomonesListos(esDoble);
            if (seLargo) {
                System.out.println("Inicio la carrera de gomones");
                System.out.println("[" + tiempo.imprimirTiempo() + "] " + v + " baja el río.");
                tiempo.esperarMinutos(15 + random.nextInt(10));
            } else {
                System.out.println("No se realizo la carrera. El horario de actividades termino.");
            }
        } finally {
            cantVisitantesEnGomonesDobles--;
            gomon.release();
        }
        retirarBolso(v, nroBolso);
    }

}