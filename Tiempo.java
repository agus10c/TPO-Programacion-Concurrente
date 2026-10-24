public class Tiempo extends Thread {

    private static final int HORA_INICIAL = 8;
    private static final int MINUTO_INICIAL = 0;
    private static final long MS_POR_MINUTO = 80;
    private static int cantMinutos = HORA_INICIAL * 60 + MINUTO_INICIAL; // total acumulado desde el arranque de la
                                                                         // simulación

    private final ParqueECO_PCS parque;
    private static final int APERTURA = 9 * 60;
    private static final int CIERRE_INGRESO = 17 * 60;
    private static final int CIERRE_ACTIVIDADES = 18 * 60;
    private static final int CIERRE_PARQUE = 19 * 60;

    public Tiempo(ParqueECO_PCS parque) {
        this.parque = parque;
    }

    public static synchronized boolean aperturaRealizada() {
        return cantMinutos >= APERTURA;
    }

    public static synchronized boolean ingresoAbierto() {
        return cantMinutos < CIERRE_INGRESO;
    }

    public static synchronized boolean actividadesAbiertas() {
        return cantMinutos < CIERRE_ACTIVIDADES;
    }

    public static synchronized boolean parqueAbierto() {
        return cantMinutos < CIERRE_PARQUE;
    }

    public static long convertirMinutos(int minutos) {
        return minutos * MS_POR_MINUTO;
    }

    public static void esperarMinutos(int minutos) throws InterruptedException {
        if (minutos <= 0) {
            System.out.println("Esperar Minutos con valor invalido: " + minutos);
            return;
        }
        Thread.sleep(convertirMinutos(minutos));
    }

    public static synchronized int getCantMinutos() {
        return cantMinutos;
    }

    public static synchronized String imprimirTiempo() {
        int minutos = cantMinutos;

        return String.format(
                "%02d:%02d",
                (minutos / 60) % 24,
                minutos % 60);
    }

    public static void informar(String s) {
        System.out.println("[" + imprimirTiempo() + "] " + s);
    }

    private static synchronized void avanzarMinuto() {
        cantMinutos++;
    }

    @Override
    public void run() {
        boolean activo = true;
        while (activo) {
            try {
                Thread.sleep(MS_POR_MINUTO);
            } catch (InterruptedException e) {
                break;
            }
            avanzarMinuto();
            try {
                if (cantMinutos == APERTURA) {
                    parque.avisarApertura();
                }
                if (cantMinutos == CIERRE_ACTIVIDADES) {
                    parque.cerrarActividades();
                }
                if (cantMinutos == CIERRE_PARQUE) {
                    parque.cerrarParque();
                    activo = false;
                }
            } catch (Exception e) {
                System.out.println("Error en notificacion del tiempo");
            }

        }
        System.out.println("Tiempo finalizado.");
    }

}