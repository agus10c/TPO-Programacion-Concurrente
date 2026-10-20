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
        try {
            while (tiempo.estaActivo()) {
                faro.asignarTobogan();
            }  
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] AdminFaro termina.");
    }

}
