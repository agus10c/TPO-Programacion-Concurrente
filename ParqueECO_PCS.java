import java.util.Random;
import java.util.concurrent.Semaphore;

public class ParqueECO_PCS {
    private final int MOLINETES = 6;
    private final Tiempo tiempo;
    private final Shop shop;
    private final Snorkel snorkel;
    private final Faro_Mirador faro;
    private final Carrera_De_Gomones gomones;
    private final Restaurante[] restaurantes;

    public static final int CIERRE_PARQUE = 19 * 60;
    public static final int CIERRE_ACTIVIDADES = 18 * 60;
    public static final int APERTURA = 8 * 60;
    public static final int CIERRE_INGRESO = 17 * 60;

    private int visitantesDentro;
    private Semaphore molinetes;
    private final Random random = new Random();

    public ParqueECO_PCS(Tiempo tiempo) {
        this.tiempo = tiempo;
        this.shop = new Shop(tiempo);
        this.snorkel = new Snorkel(tiempo);
        this.faro = new Faro_Mirador(tiempo);
        this.gomones = new Carrera_De_Gomones(tiempo);
        this.molinetes = new Semaphore(MOLINETES, true);
        restaurantes = new Restaurante[] {
                new Restaurante(tiempo, "Restaurante 1", 20),
                new Restaurante(tiempo, "Restaurante 2", 20),
                new Restaurante(tiempo, "Restaurante 3", 20)
        };
    }

    public Tiempo getTiempo() {
        return tiempo;
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

    public boolean ingresar(Visitante visitante) throws InterruptedException {
        while (tiempo.getCantMinutos() < APERTURA) {
            System.out.println(
                    "[" + tiempo.imprimirTiempo() + "] " + visitante + " espera que el parque abra para ingresar.");
            tiempo.esperarMinutos(1);
        }
        if (tiempo.getCantMinutos() >= CIERRE_INGRESO) {
            System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                    + " no pudo ingresar (el horario de ingreso ha terminado).");
            return false;
        }
        molinetes.acquire();
        int enUso = MOLINETES - molinetes.availablePermits();
        System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante + " cruza un molinete (molinetes ocupados: "
                + enUso + "/" + MOLINETES + ").");
        tiempo.esperarMinutos(1 + random.nextInt(4));
        molinetes.release();
        synchronized (this) {
            visitantesDentro++;
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                + " ingreso al parque. Cantidad de visitantes en el parque: " + visitantesDentro);
        return true;
    }

    public void salir(Visitante visitante) {
        synchronized (this) {
            visitantesDentro--;
            if (visitantesDentro <= 0) {
                this.notify();
            }
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                + " se retira del parque. Visitantes dentro del parque: " + visitantesDentro);
    }

    public void cerrarParque() throws InterruptedException {
        System.out.println("parque cerrando");
        gomones.notificarCierre();
        faro.notificarCierre();
        synchronized (this) {
            while (visitantesDentro > 0) {
                wait(); // espera que no hay ningun visitante dentro
            }
            tiempo.finalizar();
        }
        System.out.println("El parque cerro");
    }

}