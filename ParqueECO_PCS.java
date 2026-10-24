import java.util.Random;
import java.util.concurrent.Semaphore;

public class ParqueECO_PCS {
    private final int MOLINETES = 6;
    private final Shop shop;
    private final Snorkel snorkel;
    private final Faro_Mirador faro;
    private final Carrera_De_Gomones gomones;
    private final Restaurante[] restaurantes;

    private int visitantesDentro;
    private Semaphore molinetes;
    private final Random random = new Random();

    public ParqueECO_PCS() {
        this.shop = new Shop();
        this.snorkel = new Snorkel();
        this.faro = new Faro_Mirador();
        this.gomones = new Carrera_De_Gomones();
        this.molinetes = new Semaphore(MOLINETES, true);
        restaurantes = new Restaurante[] {
                new Restaurante("Restaurante 1", 20),
                new Restaurante("Restaurante 2", 20),
                new Restaurante("Restaurante 3", 20)
        };
    }

    public Snorkel getSnorkel() {
        return snorkel;
    }

    public Carrera_De_Gomones getGomones() {
        return gomones;
    }

    public Faro_Mirador getFaro() {
        return faro;
    }

    public Shop getShop() {
        return shop;
    }

    public Restaurante elegirRestaurante() {
        return restaurantes[random.nextInt(restaurantes.length)];
    }

    public synchronized void avisarApertura() {
            this.notifyAll();
    }

    public boolean ingresar(Visitante visitante) throws InterruptedException {
        while (!Tiempo.aperturaRealizada()) {
            Tiempo.informar(visitante + " espera que el parque abra para ingresar.");
            synchronized (this) {
                this.wait();
            }
        }
        molinetes.acquire();
        if (!Tiempo.ingresoAbierto()) {
            molinetes.release();
            Tiempo.informar(visitante
                    + " no pudo ingresar (el horario de ingreso ha terminado).");
            return false;
        }
        int enUso = MOLINETES - molinetes.availablePermits();
        Tiempo.informar(visitante + " cruza un molinete (molinetes ocupados: "
                + enUso + "/" + MOLINETES + ").");
        Tiempo.esperarMinutos(1 + random.nextInt(4));
        molinetes.release();
        synchronized (this) {
            visitantesDentro++;
        }
        Tiempo.informar(visitante + " ingreso al parque. Cantidad de visitantes en el parque: " + visitantesDentro);
        return true;
    }

    public void salir(Visitante visitante) throws InterruptedException {
        synchronized (this) {
            visitantesDentro--;
            if (visitantesDentro <= 0) {
                this.notifyAll();
            }
        }
        Tiempo.informar(visitante + " se retira del parque. Visitantes dentro del parque: " + visitantesDentro);
    }

    public void cerrarActividades() throws InterruptedException {
        System.out.println("Cerrando actividades del parque");
        faro.notificarCierre();
        gomones.notificarCierre();
    }

    public void cerrarParque() throws InterruptedException {
        Tiempo.informar("Parque cerrando");
        for (int i = 0; i < restaurantes.length; i++) {
            restaurantes[i].notificarCierre();
        }
        synchronized (this) {
            while (visitantesDentro > 0) {
                wait(); // espera que no hay ningun visitante dentro
            }
        }
        System.out.println("El parque cerro");
    }

}