import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Faro_Mirador implements Actividad {

    private static final int CAPACIDAD_ESCALERA = 20;

    private final Random random = new Random();

    private final Lock lock = new ReentrantLock(true);
    private final Condition hayVisitantes = lock.newCondition();
    private final Condition hayLugarEscalera = lock.newCondition();
    private final Condition hayAsignacion = lock.newCondition();
    private final Condition asignacionLeida = lock.newCondition();
    private final Queue<Visitante> colaCima = new LinkedList<>();
    private final Semaphore tobogan1;
    private final Semaphore tobogan2;
    private int toboganAsignado = 2;
    private int personasEnEscalera = 0;
    private boolean asignado = false;
    private Visitante visitanteAsignado = null;

    public Faro_Mirador() {
        this.tobogan1 = new Semaphore(1, true);
        this.tobogan2 = new Semaphore(1, true);
    }

    public Visitante elegirVisitante() throws InterruptedException {
        lock.lock();
        try {
            return colaCima.poll();
        } finally {
            lock.unlock();
        }
    }

    public void asignarTobogan() throws InterruptedException {
        lock.lock();
        try {
            while (colaCima.isEmpty() && Tiempo.actividadesAbiertas()) {
                hayVisitantes.await();
            }
            if (!Tiempo.actividadesAbiertas()) {
                hayAsignacion.signalAll();
                asignacionLeida.signalAll();
                return;
            }
            Visitante v = colaCima.poll();
            visitanteAsignado = v;
            asignado = true;
            toboganAsignado = (toboganAsignado == 1) ? 2 : 1;
            Tiempo.informar("El admin le asigno el tobogan " + toboganAsignado + " a " + v);
            // Despertar a los visitantes que esperan su asignación.
            hayAsignacion.signal();
            // Esperar hasta que el visitante seleccionado lea el valor.
            while (asignado && Tiempo.actividadesAbiertas()) {
                asignacionLeida.await();
            }

            if (!Tiempo.actividadesAbiertas()) {
                hayAsignacion.signalAll();
                return;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void realizarActividad(Visitante v) throws InterruptedException {
        if (!Tiempo.actividadesAbiertas()) {
            return;
        }
        lock.lock();
        try {
            while (personasEnEscalera >= CAPACIDAD_ESCALERA && Tiempo.actividadesAbiertas()) {
                hayLugarEscalera.await();
            }
            if (!Tiempo.actividadesAbiertas()) {
                System.out.println("El horario de actividades termino mientras " + v
                        + " esperaba para subir las escaleras caracol");
                return;
            }
            personasEnEscalera++;
            Tiempo.informar( v + " Sube las escaleras de caracol.");
        } finally {
            lock.unlock();
        }

        Tiempo.esperarMinutos(3);

        lock.lock();
        try {
            personasEnEscalera--;
            hayLugarEscalera.signal();
            colaCima.add(v);
            hayVisitantes.signal();
        } finally {
            lock.unlock();
        }
        int tobogan;
        lock.lock();
        try {
            while ((visitanteAsignado != v || !asignado)
                    && Tiempo.actividadesAbiertas()) {
                hayAsignacion.await();
            }

            if (!Tiempo.actividadesAbiertas()) {
                System.out.println("Las actividades cerrraron mientras " + v + " esperaba la asignacion de un tobogan");
                return;
            }
            tobogan = toboganAsignado;

            asignado = false;
            visitanteAsignado = null;

            asignacionLeida.signal();

        } finally {
            lock.unlock();
        }

        if (tobogan == 1) {
            tobogan1.acquire();
            Tiempo.informar(v + " se tira por el tobogan 1.");
            Tiempo.esperarMinutos(2 + random.nextInt(2));
            tobogan1.release();
        } else {
            tobogan2.acquire();
            Tiempo.informar( v + " se tira por el tobogan 2.");
            Tiempo.esperarMinutos(2 + random.nextInt(2));
            tobogan2.release();
        }
        Tiempo.informar(v + " llega a la pileta.");
    }

    public void notificarCierre() {
        lock.lock();
        try {
            hayVisitantes.signalAll();
            hayAsignacion.signalAll();
            asignacionLeida.signalAll();
            hayLugarEscalera.signalAll();
        } finally {
            lock.unlock();
        }
    }
}