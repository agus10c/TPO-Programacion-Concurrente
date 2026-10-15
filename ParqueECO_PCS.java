import java.util.Random;
import java.util.concurrent.Semaphore;

public class ParqueECO_PCS {
    private static final int MOLINETES = 6;
    private final Tiempo tiempo;
    private final Shop shop;
    private final Snorkel snorkel;
    private final Faro_Mirador faro;
    private final Carrera_De_Gomones gomones;
    private final Restaurante[] restaurantes;

    private Semaphore molinetes;
    private final Random random = new Random();

    private final int APERTURA;
    private final int CIERRE_INGRESO;
    private final int CIERRE_ACTIVIDADES;
    private final int CIERRE_PARQUE;

    public ParqueECO_PCS(Tiempo tiempo) {
        this.tiempo = tiempo;

        APERTURA = 8 * 60;
        CIERRE_INGRESO = 17 * 60;
        CIERRE_ACTIVIDADES = 18 * 60;
        CIERRE_PARQUE = 19 * 60;

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

    public boolean parqueAbierto() {

        int aimprimirTiempo = tiempo.getCantMinutos();

        return aimprimirTiempo >= APERTURA &&
                aimprimirTiempo < CIERRE_PARQUE;
    }

    public boolean actividadesAbiertas() {

        int aimprimirTiempo = tiempo.getCantMinutos();

        return aimprimirTiempo >= APERTURA &&
                aimprimirTiempo < CIERRE_ACTIVIDADES;
    }

    public boolean ingresar(Visitante visitante) throws InterruptedException {
        while (!parqueAbierto()) {
            System.out.println(
                    "[" + tiempo.imprimirTiempo() + "] " + visitante + " espera que el parque abra para ingresar.");
            tiempo.esperarMinutos(1);
        }
        if (tiempo.getCantMinutos() >= CIERRE_INGRESO) {
            System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                    + " no pudo ingresar (el imprimirTiemporio de ingreso ha terminado).");
            return false; // el parque está cerrado
        }
        molinetes.acquire();
        int enUso = MOLINETES - molinetes.availablePermits();
        System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante + " cruza un molinete (molinetes ocupados: "
                + enUso + "/" + ParqueECO_PCS.MOLINETES + ").");
        tiempo.esperarMinutos(1 + random.nextInt(4));
        molinetes.release();
        System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante + " ingreso al parque.");
        return true;
    }

    public void salir(Visitante visitante) {
        System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante + " se retira del parque.");
    }

}