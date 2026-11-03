import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

public class Colectivo extends Thread {

    public static final int CAPACIDAD_MAXIMA = 25;
    private static final int TIEMPO_DE_ESPERA_MINUTOS = 20;

    private final Random random = new Random();

    private final Lock lock = new ReentrantLock();
    private final Condition hayLugar = lock.newCondition();
    private final Condition llego = lock.newCondition();
    private final Condition puedeViajar = lock.newCondition();

    private int cantPasajeros;
    private boolean sePuedeSubir;
    private boolean sePuedeBajar;

    public Colectivo() {
        this.cantPasajeros = 0;
        this.sePuedeSubir = true;
        this.sePuedeBajar = false;
    }

    public boolean subirYEsperar(Visitante v) throws InterruptedException {
        lock.lock();
        try {
            puedeViajar.signal();
            while ((!sePuedeSubir || cantPasajeros >= CAPACIDAD_MAXIMA)
                    && Tiempo.parqueAbierto()) {
                hayLugar.await();
            }
            if (!Tiempo.parqueAbierto())
                return false;
            cantPasajeros++;
            Tiempo.informar(v + " sube al colectivo de tour (" + cantPasajeros + "/" + CAPACIDAD_MAXIMA + ").");

            if (cantPasajeros == CAPACIDAD_MAXIMA) {
                puedeViajar.signalAll();
            }

            while (!sePuedeBajar && Tiempo.parqueAbierto()) {
                llego.await();
            }
            if (!Tiempo.parqueAbierto())
                return false;
            cantPasajeros--;
            Tiempo.informar(v + " baja del colectivo de tour (" + cantPasajeros + "/" + CAPACIDAD_MAXIMA + ").");

            if (cantPasajeros == 0) {
                puedeViajar.signalAll();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    private boolean esperarPasajeros() throws InterruptedException {
        lock.lock();
        try {
            while (cantPasajeros == 0 && Tiempo.parqueAbierto()) {
                puedeViajar.await();
            }
            if (!Tiempo.parqueAbierto()) {
                return false;
            }
            int inicio = Tiempo.getCantMinutos();
            while (cantPasajeros < CAPACIDAD_MAXIMA
                    && (Tiempo.getCantMinutos() - inicio) < TIEMPO_DE_ESPERA_MINUTOS
                    && Tiempo.parqueAbierto()) {
                puedeViajar.await(Tiempo.convertirMinutos(1), TimeUnit.MILLISECONDS);
            }
            if (!Tiempo.parqueAbierto()) {
                return false;
            }
            sePuedeSubir = false;
            Tiempo.informar("Colectivo de tour parte con " + cantPasajeros + " pasajeros.");
            return true;
        } finally {
            lock.unlock();
        }
    }

    private boolean llegar() throws InterruptedException {

        Tiempo.informar("Colectivo tour llegó al parque.");
        lock.lock();
        try {
            sePuedeBajar = true;

            llego.signalAll();

            while (cantPasajeros > 0 && Tiempo.parqueAbierto()) {
                puedeViajar.await();
            }

            if (!Tiempo.parqueAbierto())
                return false;

            sePuedeBajar = false;
            return true;
        } finally {
            lock.unlock();
        }

    }

    public void notificarCierre() {
        lock.lock();
        try {
            hayLugar.signalAll();
            puedeViajar.signalAll();
            llego.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        try {
            while (Tiempo.parqueAbierto()) {
                if (!esperarPasajeros())
                    return;

                Tiempo.esperarMinutos(10 + random.nextInt(3));
                if (!llegar()) {
                    return;
                }

                Tiempo.esperarMinutos(10 + random.nextInt(3));

                Tiempo.informar("Colectivo de tour volvio a buscar mas visitantes.");
                lock.lock();
                sePuedeSubir = true;
                hayLugar.signalAll();
                lock.unlock();
            }
            System.out.println("Colectivo de tour termina");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}