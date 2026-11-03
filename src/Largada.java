import java.util.concurrent.CountDownLatch;

public class Largada {
    private final CountDownLatch latch;

    private boolean cambiarLargada = true;

    private boolean abortada = false;

    public Largada(int cantidad) {
        this.latch = new CountDownLatch(cantidad);
    }

    public void esperarLargada() throws InterruptedException {
        latch.countDown();
        if (latch.getCount() == 0)
            System.out.println("Largada lista. Comienza la carrera!");
        latch.await();
    }

    public synchronized boolean cambiarLargada() {
        return cambiarLargada;
    }

    public synchronized void cambiada() {
        cambiarLargada = false;
    }

    public synchronized boolean getAbortada() {
        return abortada;
    }

    public synchronized void abortar() {
        abortada = true;
        while (latch.getCount() > 0) {
            latch.countDown();
        }
    }

}
