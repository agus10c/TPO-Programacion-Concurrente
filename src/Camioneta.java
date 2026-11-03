import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Camioneta extends Thread {

    private final Lock lockBolsos = new ReentrantLock(true);
    private final Condition hayBolsosPendientes = lockBolsos.newCondition();
    private final Condition hayBolsosParaRetrirar = lockBolsos.newCondition();
    private final Random random = new Random();
    private final List<Integer> bolsosPendientes = new LinkedList<>();
    private final Set<Integer> bolsosParaRetirar = new HashSet<>();
    private boolean terminar = false;

    public Camioneta() {
        super("Camioneta");
    }

    public int guardarBolso(Visitante v) {
        int nroBolso = v.getId();

        lockBolsos.lock();
        try {
            if (terminar) {
                return -1;
            }
            bolsosPendientes.add(nroBolso);
            hayBolsosPendientes.signalAll();

        } finally {
            lockBolsos.unlock();
        }
        Tiempo.informar(v + " guarda su bolso N°" + nroBolso + ".");
        return nroBolso;
    }

    public void retirarBolso(Visitante v, int nroBolso) throws InterruptedException {
        lockBolsos.lock();
        try {
            while (!bolsosParaRetirar.contains(nroBolso)) {
                hayBolsosParaRetrirar.await();
            }
            bolsosParaRetirar.remove(nroBolso);
            Tiempo.informar(v + " retira su bolso N°" + nroBolso + ".");
        } finally {
            lockBolsos.unlock();
        }
    }

    public List<Integer> retirarBolsosPendientes() throws InterruptedException {
        try {
            lockBolsos.lock();
            while (bolsosPendientes.isEmpty() && !terminar) {
                hayBolsosPendientes.await();
            }
            if (bolsosPendientes.isEmpty())
                return null;

            List<Integer> copia = new LinkedList<>(bolsosPendientes);
            bolsosPendientes.clear();

            return copia;
        } finally {
            lockBolsos.unlock();
        }

    }

    public void depositarBolso(int nroBolso) {
        lockBolsos.lock();
        try {
            bolsosParaRetirar.add(nroBolso);
            hayBolsosParaRetrirar.signalAll(); // avisa a los visitantes que esperan para retirar

        } finally {
            lockBolsos.unlock();
        }
    }

    public int cantidadBolsosPendientes() {
        lockBolsos.lock();
        try {
            return bolsosPendientes.size();
        } finally {
            lockBolsos.unlock();
        }
    }

    public void notificarCierre() {
        lockBolsos.lock();
        try {
            terminar = true;
            hayBolsosPendientes.signalAll();
        } finally {
            lockBolsos.unlock();
        }
    }

    private boolean terminado() {
        lockBolsos.lock();
        try {
            return terminar;
        } finally {
            lockBolsos.unlock();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (cantidadBolsosPendientes() < 6 && !terminado()) {
                    Tiempo.esperarMinutos(10);
                }
                List<Integer> bolsos = retirarBolsosPendientes();
                if (bolsos == null)
                    break;
                Tiempo.informar("Camioneta lleva " + bolsos.size() + " bolsos.");
                Tiempo.esperarMinutos(5 + random.nextInt(3));
                for (int nro : bolsos) {
                    depositarBolso(nro);
                }
                Tiempo.informar("La camioneta dejó los bolsos para que los retiren.");

                if (!terminado())
                    Tiempo.esperarMinutos(5 + random.nextInt(3));
            }
            System.out.println("Camioneta finalizo");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
