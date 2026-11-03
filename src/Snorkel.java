import java.util.Random;
import java.util.concurrent.Semaphore;

public class Snorkel implements Actividad {

    private static final int CANTIDAD_EQUIPOS = 10;
    private static final int CANTIDAD_ASISTENTES = 2;

    private final Semaphore equiposCompletos;
    private final Semaphore asistentes;

    private final Random random = new Random();

    public Snorkel() {
        this.equiposCompletos = new Semaphore(CANTIDAD_EQUIPOS, true);
        this.asistentes = new Semaphore(CANTIDAD_ASISTENTES, true);
    }

    private boolean solicitarEquipo(Visitante visitante) throws InterruptedException {
        Tiempo.informar(visitante + " espera a que un asistente lo atienda.");
        asistentes.acquire();
        if (!Tiempo.actividadesAbiertas()) {
            return false;
        }
        try {
            Tiempo.informar(visitante + " esta siendo atendido por un asistente de snorkel.");
            Tiempo.esperarMinutos(1 + random.nextInt(3));

            if (equiposCompletos.availablePermits() == 0) {
                Tiempo.informar(visitante + " espera a que haya equipo disponible.");
            }
            equiposCompletos.acquire();
            if (!Tiempo.actividadesAbiertas()) {
                equiposCompletos.release();
                return false;
            }
            Tiempo.informar(visitante + " recibió snorkel, salvavidas y patas de rana.");
            return true;
        } finally {
            asistentes.release();
        }
    }

    private void devolverEquipo(Visitante visitante) {
        equiposCompletos.release();
        Tiempo.informar(visitante + " devolvió el equipo de snorkel.");
    }

    @Override
    public void realizarActividad(Visitante visitante) throws InterruptedException {
        if (!Tiempo.actividadesAbiertas()) {
            return;
        }
        Tiempo.informar(visitante + " Va a hacer Snorkel");
        if (solicitarEquipo(visitante)) {
            Tiempo.informar(visitante + " realiza snorkel.");
            Tiempo.esperarMinutos(10 + (random.nextInt(11)));
            devolverEquipo(visitante);
        }
    }

}