import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.Random;

public class Colectivo extends Thread {

    public static final int CAPACIDAD_MAXIMA = 25;
    private static final int TIEMPO_DE_ESPERA_MINUTOS = 20;

    private final Tiempo tiempo;

    private final Random random = new Random();

    private final Lock lock = new ReentrantLock();
    private final Condition hayLugar = lock.newCondition();
    private final Condition llego = lock.newCondition();
    private final Condition lleno = lock.newCondition();

    private final ArrayList<Visitante> pasajeros = new ArrayList<>();
    private boolean viajando = false;
    private boolean enParque = false;

    public Colectivo(Tiempo tiempo, int id) {
        super("Colectivo-" + id);
        this.tiempo = tiempo;
    }

    public void subirYEsperar(Visitante v) throws InterruptedException {
        lock.lock();
        try {
            // Esperar a que haya lugar
            while ((viajando || pasajeros.size() >= CAPACIDAD_MAXIMA) && tiempo.estaActivo()) {
                hayLugar.await();
            }
            if (!tiempo.estaActivo())
                return;

            pasajeros.add(v);
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + getName() + ": sube " + v
                    + " (" + pasajeros.size() + "/" + CAPACIDAD_MAXIMA + ").");

            if (pasajeros.size() == CAPACIDAD_MAXIMA) {
                lleno.signalAll(); // despierta al colectivo (está lleno)
            }

            // Esperar a que el colectivo llegue
            while (!enParque) {
                llego.await();
            }

            // El visitante se baja
            pasajeros.remove(v);
            if (pasajeros.isEmpty()) {
                // Si era el último, avisar al colectivo
                hayLugar.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    private void esperar() throws InterruptedException {
        lock.lock();
        try {
            int inicio = tiempo.getCantMinutos();
            while (pasajeros.size() < CAPACIDAD_MAXIMA
                    && (tiempo.getCantMinutos() - inicio) < TIEMPO_DE_ESPERA_MINUTOS
                    && tiempo.estaActivo()) {
                lleno.await(tiempo.convertirMinutos(1), TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean hayPasajeros() {
        lock.lock();
        try {
            return !pasajeros.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    private void partir() {
        lock.lock();
        try {
            viajando = true;
        } finally {
            lock.unlock();
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + getName() + ": parte con " + cantidadPasajeros() + " pasajeros.");
    }

    private void llegar() {
        lock.lock();
        try {
            viajando = false;
            enParque = true;
            llego.signalAll();
        } finally {
            lock.unlock();
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + getName() + ": llegó al parque.");
    }

    private void esperarQueSeVacie() throws InterruptedException {
        lock.lock();
        try {
            while (!pasajeros.isEmpty() && tiempo.estaActivo()) {
                hayLugar.await();
            }
            enParque = false;
            hayLugar.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private int cantidadPasajeros() {
        lock.lock();
        try {
            return pasajeros.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public void run() {
        try {
            while (tiempo.estaActivo()) {

                esperar();

                if (!hayPasajeros())
                    continue;

                partir();

                tiempo.esperarMinutos(10 + random.nextInt(3));

                llegar();

                esperarQueSeVacie();

                tiempo.esperarMinutos(10 + random.nextInt());
                System.out.println("[" + tiempo.imprimirTiempo() + "] "
                        + getName() + ": vuelve a la ciudad.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}