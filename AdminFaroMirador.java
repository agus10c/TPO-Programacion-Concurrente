public class AdminFaroMirador extends Thread {

    private final Tiempo tiempo;
    private final Faro_Mirador faro;

    public AdminFaroMirador(Tiempo tiempo, Faro_Mirador faro) {
        super("AdminFaro");
        this.tiempo = tiempo;
        this.faro = faro;
    }

    @Override
    public void run() {
        int toboganAelegir = 1;
        try {
            while (tiempo.estaActivo()) {
                Visitante v = faro.elegirVisitante();
                if (v == null)
                    continue;
                System.out.println("[" + tiempo.imprimirTiempo() + "] "
                        + "AdminFaro asigna " + v + " al Tobogán " + toboganAelegir + ".");

                faro.asignarTobogan(toboganAelegir);
                toboganAelegir = (toboganAelegir == 1) ? 2 : 1;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] AdminFaro termina.");
    }
}
