public class AdminFaroMirador extends Thread {

    private final Faro_Mirador faro;

    public AdminFaroMirador(Faro_Mirador faro) {
        super("AdminFaro");
        this.faro = faro;
    }

    @Override
    public void run() {
        try {
            while (Tiempo.actividadesAbiertas()) {
                faro.asignarTobogan();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Admin Faro termina");
    }

}
