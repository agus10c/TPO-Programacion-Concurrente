import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Faro_Mirador implements Actividad {

    private static final int CAPACIDAD_ESCALERA = 20;

    private final Tiempo tiempo;
    private final BlockingQueue<Visitante> colaCima;
    private final Semaphore tobogan1;
    private final Semaphore tobogan2;
    private final Exchanger<Integer> exchanger = new Exchanger<>();

    private final Random random = new Random();

    public Faro_Mirador(Tiempo tiempo) {
        this.tiempo = tiempo;
        this.colaCima = new ArrayBlockingQueue<>(CAPACIDAD_ESCALERA);
        this.tobogan1 = new Semaphore(1, true);
        this.tobogan2 = new Semaphore(1, true);
    }

    public Visitante elegirVisitante() throws InterruptedException {
        return colaCima.poll(100, TimeUnit.MILLISECONDS);
    }

    public void asignarTobogan(int numero) throws InterruptedException {
        exchanger.exchange(numero);
    }

    @Override
    public void realizarActividad(Visitante v) throws InterruptedException {
        if (tiempo.getCantMinutos() >= ParqueECO_PCS.CIERRE_ACTIVIDADES) {
            return;
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " va al faro Mirador.");
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " Sube las escaleras de caracol.");
        tiempo.esperarMinutos(3);
        colaCima.put(v);

        int toboganAsignado = exchanger.exchange(null);
        if (toboganAsignado == 1) {
            tobogan1.acquire();
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " se tira por el tobogan 1.");
            tiempo.esperarMinutos(2 + random.nextInt(2));
            tobogan1.release();

        } else {
            tobogan2.acquire();
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " se tira por el tobogan 2.");
            tiempo.esperarMinutos(2 + random.nextInt(2));
            tobogan2.release();
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " llega a la pileta.");
    }

}