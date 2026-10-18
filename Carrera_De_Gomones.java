import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Carrera_De_Gomones implements Actividad {

    private static final int CANT_BICICLETAS = 10;
    private static final int CAPACIDAD_TREN = 15;
    private static final int CANT_GOMONES_INDIVIDUALES = 8;
    private static final int CANT_GOMONES_DOBLES = 5;
    private static final int GOMONES_PARA_LARGADA = 6;

    private final Tiempo tiempo;
    private final Random random = new Random();

    private final Semaphore bicicletas;
    private final Semaphore tren;
    private final Semaphore gomonesIndividuales;
    private final Semaphore gomonesDobles;
    private final CyclicBarrier largada;

    private final List<Integer> bolsosPendientes = new ArrayList<>();

    private final Map<Integer, Boolean> bolsosParaRetirar = new HashMap<>();

    public Carrera_De_Gomones(Tiempo tiempo) {
        this.tiempo = tiempo;
        this.bicicletas = new Semaphore(CANT_BICICLETAS, true);
        this.tren = new Semaphore(CAPACIDAD_TREN, true);
        this.gomonesIndividuales = new Semaphore(CANT_GOMONES_INDIVIDUALES, true);
        this.gomonesDobles = new Semaphore(CANT_GOMONES_DOBLES, true);
        this.largada = new CyclicBarrier(GOMONES_PARA_LARGADA);
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

        synchronized (bolsosPendientes) {
            bolsosPendientes.add(nroBolso);
            bolsosPendientes.notifyAll();
        }
        return nroBolso;
    }

    public List<Integer> retirarBolsosPendientes() throws InterruptedException { // La camioneta busca los bolsos para
                                                                                 // // llevar
        synchronized (bolsosPendientes) {
            while (bolsosPendientes.isEmpty()) {
                if (!tiempo.estaActivo())
                    return null;
                bolsosPendientes.wait(100);
            }
            List<Integer> copia = new ArrayList<>(bolsosPendientes);
            bolsosPendientes.clear();
            return copia;
        }
    }

    public void depositarBolso(int nroBolso) {
        synchronized (bolsosParaRetirar) {
            bolsosParaRetirar.put(nroBolso, true);
            bolsosParaRetirar.notifyAll(); // avisa a los visitantes que esperan
        }
    }

    private void retirarBolso(Visitante v, int nroBolso) throws InterruptedException { // Los visitantes retiran los
                                                                                       // bolsos
        synchronized (bolsosParaRetirar) {
            while (!bolsosParaRetirar.containsKey(nroBolso)) {
                if (!tiempo.estaActivo()) {
                    System.out.println("[" + tiempo.imprimirTiempo() + "] "
                            + v + " El parque cerro. Le devolvieron su bolso.");
                    return;
                }
                bolsosParaRetirar.wait(100);
            }
            bolsosParaRetirar.remove(nroBolso);
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " retira su bolso N°" + nroBolso + ".");
        }
    }

    public int cantidadBolsosPendientes() {
        synchronized (bolsosPendientes) {
            return bolsosPendientes.size();
        }
    }

    @Override
    public void realizarActividad(Visitante v) throws InterruptedException {
        if (tiempo.getCantMinutos() >= ParqueECO_PCS.CIERRE_ACTIVIDADES) {
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
        try {
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " toma un gomón " + tipo + ".");

            try {
                largada.await(500, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("[" + tiempo.imprimirTiempo() + "] "
                        + v + " no se largó (faltaban gomones).");
                return;
            } catch (BrokenBarrierException e) {
                System.out.println("[" + tiempo.imprimirTiempo() + "] "
                        + v + " no se largó (barrera rota).");
                return;
            }
            System.out.println("[" + tiempo.imprimirTiempo() + "] " + v + " se larga a bajar el río.");

            tiempo.esperarMinutos(15 + random.nextInt(10));

            System.out.println("[" + tiempo.imprimirTiempo() + "] " + v + " llega al final del río.");

        } finally {
            gomon.release();
        }

        retirarBolso(v, nroBolso);
    }

}