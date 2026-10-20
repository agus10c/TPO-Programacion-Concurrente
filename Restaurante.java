import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class Restaurante {

    private final Tiempo tiempo;
    private final String nombre;
    private final int CAPACIDAD;
    private final Random random = new Random();
    private final Semaphore mutex;

    private int ocupadas = 0;
    private final BlockingQueue<Visitante> colaEspera = new LinkedBlockingQueue<>();

    public Restaurante(Tiempo tiempo, String nombre, int capacidad) {
        this.tiempo = tiempo;
        this.nombre = nombre;
        this.CAPACIDAD = capacidad;
        this.mutex = new Semaphore(1);
    }

    public boolean almorzar(Visitante v) throws InterruptedException {
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " Va a almorzar a restaurante " + nombre);
        return comer(v, "almuerzo");
    }

    public boolean merendar(Visitante v) throws InterruptedException {
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " Va a merendar a restaurante " + nombre);
        return comer(v, "merienda");
    }

    private boolean comer(Visitante v, String comida) throws InterruptedException {
        if (!entrar(v))
            return false;
        try {
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " come " + comida + " en " + nombre + ".");
            tiempo.esperarMinutos(10 + random.nextInt(10));
        } finally {
            salir(v, comida);
        }
        return true;
    }

    private boolean entrar(Visitante v) throws InterruptedException {
        if (!tiempo.parqueAbierto()) {
            return false;
        }
        mutex.acquire();
        if (ocupadas < CAPACIDAD) {
            ocupadas++;
            mutex.release();
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " entra a " + nombre + ".");
        } else {
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " espera en la cola para entrar en " + nombre + ".");
            colaEspera.add(v);
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " entra a " + nombre + ".");
        }
        return true;
    }

    private void salir(Visitante v, String comida) throws InterruptedException {
        mutex.acquire();
        ocupadas--;
        colaEspera.take();
        mutex.release();
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " terminó " + comida + " en " + nombre + ".");
        this.notifyAll();
    }

}