import java.util.Random;
import java.util.concurrent.Semaphore;

public class Snorkel implements Actividad {

    private static final int CANTIDAD_EQUIPOS = 10;
    private static final int CANTIDAD_ASISTENTES = 2;

    private final Tiempo tiempo;
    private final Semaphore equiposCompletos;
    private final Semaphore asistentes;

    private final Random random = new Random();

    public Snorkel(Tiempo tiempo) {
        this.tiempo = tiempo;
        this.equiposCompletos = new Semaphore(CANTIDAD_EQUIPOS, true);
        this.asistentes = new Semaphore(CANTIDAD_ASISTENTES, true);
    }

    private void solicitarEquipo(Visitante visitante) throws InterruptedException {
        System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                + " espera a que un asistente lo atienda.");

        asistentes.acquire();
        try {
            System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                    + " esta siendo atendido por un asistente de snorkel.");
            tiempo.esperarMinutos(1 + random.nextInt(3));

            if (equiposCompletos.availablePermits() == 0) {
                System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                        + " espera a que haya equipo disponible.");
            }
            equiposCompletos.acquire();

            System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                    + " recibió snorkel, salvavidas y patas de rana.");
        } finally {
            asistentes.release();
        }
    }

    private void devolverEquipo(Visitante visitante) {
        equiposCompletos.release();
        System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                + " devolvió el equipo de snorkel.");
    }

    @Override
    public boolean realizarActividad(Visitante visitante) throws InterruptedException {
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + visitante + " Va a hacer Snorkel");
        solicitarEquipo(visitante);
        try {
            System.out.println("[" + tiempo.imprimirTiempo() + "] " + visitante
                    + " realiza snorkel.");
            tiempo.esperarMinutos(10 + (random.nextInt(11)));
        } finally {
            devolverEquipo(visitante);
        }
        return true;
    }

}