import java.util.Random;

public class Generador_De_Visitantes extends Thread {
    private static final int TIEMPO_DE_GENERACION_MIN = 50;
    private static final int PROB_TOUR = 20; // % de probabilidad de tour
    private static final int MIN_PASAJEROS_TOUR = 10;
    private static final int MAX_PASAJEROS_TOUR = 40;

    private final ParqueECO_PCS parque;
    private final Colectivo colectivo;
    private final Random random = new Random();

    public Generador_De_Visitantes(ParqueECO_PCS parque) {
        super("Generador_De_Visitantes");
        this.parque = parque;
        this.colectivo = new Colectivo();
        colectivo.start();
    }

    private void generarTour() {
        int cantidad = MIN_PASAJEROS_TOUR + random.nextInt(MAX_PASAJEROS_TOUR - MIN_PASAJEROS_TOUR + 1);
        Tiempo.informar("Van " + cantidad + " visitantes de tour.");
        for (int i = 0; i < cantidad; i++) {
            Visitante v = new Visitante(parque, colectivo);
            new Thread(v, "Visitante-" + v.getId()).start();
        }
    }

    private void generarParticulares() {
        int grupo = 1 + random.nextInt(5);
        Tiempo.informar("Llega un grupo de " + grupo + " visitantes particular al parque.");

        for (int i = 0; i < grupo; i++) {
            Visitante v = new Visitante(parque);
            new Thread(v, "Visitante-" + v.getId()).start();
        }
    }

    @Override
    public void run() {
        try {
            while (Tiempo.parqueAbierto()) {
                if (random.nextInt(100) < PROB_TOUR) {
                    generarTour();
                } else {
                    generarParticulares();
                }
                Tiempo.esperarMinutos(TIEMPO_DE_GENERACION_MIN + random.nextInt(20));
            }
            colectivo.notificarCierre();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Fin de la generacion de visitantes");
    }

}
