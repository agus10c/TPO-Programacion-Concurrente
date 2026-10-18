import java.util.Random;

public class Generador_De_Visitantes extends Thread {
    private static final int TIEMPO_DE_GENERACION_MIN = 40;
    private static final int PROB_TOUR = 20; // % de probabilidad de tour
    private static final int CANT_COLECTIVOS = 2;
    private static final int MIN_PASAJEROS_TOUR = 10;
    private static final int MAX_PASAJEROS_TOUR = 25;

    private final ParqueECO_PCS parque;
    private final Colectivo[] colectivos;
    private final Random random = new Random();
    private int siguienteColectivo = 0;

    public Generador_De_Visitantes(ParqueECO_PCS parque) {
        super("Generador_De_Visitantes");
        this.parque = parque;

        this.colectivos = new Colectivo[CANT_COLECTIVOS];
        for (int i = 0; i < CANT_COLECTIVOS; i++) {
            colectivos[i] = new Colectivo(parque.getTiempo(), i + 1);
            colectivos[i].start();
        }
    }

    private Colectivo siguienteColectivo() {
        Colectivo c = colectivos[siguienteColectivo];
        siguienteColectivo = (siguienteColectivo + 1) % colectivos.length;
        return c;
    }

    @Override
    public void run() {
        try {
            while (parque.getTiempo().estaActivo()) {
                if (random.nextInt(100) < PROB_TOUR) {
                    generarTour();
                } else {
                    generarParticulares();
                }

                parque.getTiempo().esperarMinutos(
                        TIEMPO_DE_GENERACION_MIN + random.nextInt(20));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("[" + parque.getTiempo().imprimirTiempo()
                + "] Fin de la generación de visitantes.");
    }

    private void generarTour() {
        int cantidad = MIN_PASAJEROS_TOUR + random.nextInt(MAX_PASAJEROS_TOUR - MIN_PASAJEROS_TOUR + 1);
        System.out.println("[" + parque.getTiempo().imprimirTiempo()
                + "] Llegan " + cantidad + " visitantes de tour.");

        for (int i = 0; i < cantidad; i++) {
            Colectivo c = siguienteColectivo();
            Visitante v = new Visitante(parque, c);
            new Thread(v, "Visitante-" + v.getId()).start();
        }
    }

    private void generarParticulares() {
        System.out.println("[" + parque.getTiempo().imprimirTiempo()
                + "] Llega un visitantes particular al parque.");
        Visitante v = new Visitante(parque);
        new Thread(v, "Visitante-" + v.getId()).start();
    }
}
