import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class Restaurante {

    private final Tiempo tiempo;
    private final String nombre;
    private final int CAPACIDAD;
    private final Random random = new Random();

    private int ocupadas = 0;
    private final Queue<Visitante> cola = new ArrayDeque<>();

    public Restaurante(Tiempo tiempo, String nombre, int capacidad) {
        this.tiempo = tiempo;
        this.nombre = nombre;
        this.CAPACIDAD = capacidad;
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

    private synchronized boolean entrar(Visitante v) throws InterruptedException {
        if (ocupadas < CAPACIDAD && cola.isEmpty()) {
            ocupadas++;
            System.out.println("[" + tiempo.imprimirTiempo() + "] "
                    + v + " entra a " + nombre + ".");
            return true;
        }

        cola.add(v);
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " espera en la cola para entrar en " + nombre + ".");

        while (!(cola.peek() == v && ocupadas < CAPACIDAD)) {
            if (!tiempo.estaActivo()) {
                cola.remove(v);
                return false;
            }
            this.wait();
        }

        cola.poll();
        ocupadas++;
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " entra a " + nombre + ".");
        return true;
    }

    private synchronized void salir(Visitante v, String comida) {
        ocupadas--;
        System.out.println("[" + tiempo.imprimirTiempo() + "] "
                + v + " terminó " + comida + " en " + nombre + ".");
        this.notifyAll();
    }

}