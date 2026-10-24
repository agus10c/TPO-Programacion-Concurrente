import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

public class Restaurante {

    private final String nombre;
    private final int CAPACIDAD;
    private final Random random = new Random();

    private int ocupadas = 0;
    private final Queue<Visitante> cola = new ArrayDeque<>();

    public Restaurante(String nombre, int capacidad) {
        this.nombre = nombre;
        this.CAPACIDAD = capacidad;
    }

    public boolean almorzar(Visitante v) throws InterruptedException {
        Tiempo.informar(v + " Va a almorzar a " + nombre);
        return comer(v, "almuerzo");
    }

    public boolean merendar(Visitante v) throws InterruptedException {
        Tiempo.informar(v + " Va a merendar a " + nombre);
        return comer(v, "merienda");
    }

    private boolean comer(Visitante v, String comida) throws InterruptedException {
        if (!entrar(v))
            return false;

        try {
            Tiempo.informar(v + " come " + comida + " en " + nombre + ".");
            Tiempo.esperarMinutos(10 + random.nextInt(10));
        } finally {
            salir(v, comida);
        }
        return true;
    }

    private synchronized boolean entrar(Visitante v) throws InterruptedException {
        if (!Tiempo.parqueAbierto()) {
            return false;
        }
        if (ocupadas < CAPACIDAD && cola.isEmpty()) {
            ocupadas++;
            Tiempo.informar(v + " entra a " + nombre + ".");
            return true;
        }

        cola.add(v);
        Tiempo.informar(v + " espera en la cola para entrar en " + nombre + ".");

        while (!(cola.peek() == v && ocupadas < CAPACIDAD)) {
            if (!Tiempo.parqueAbierto()) {
                cola.remove(v);
                System.out.println("cerro el parque mientras " + v + " esperaba en la cola");
                return false;
            }
            this.wait();
        }

        cola.poll();
        ocupadas++;
        Tiempo.informar(v + " entra a " + nombre + ".");
        return true;
    }

    private synchronized void salir(Visitante v, String comida) {
        ocupadas--;
        Tiempo.informar(v + " terminó " + comida + " en " + nombre + ".");
        this.notifyAll();
    }

    public synchronized void notificarCierre() {
        this.notifyAll();
    }

}