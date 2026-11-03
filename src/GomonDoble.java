import java.util.Random;

public class GomonDoble {
    private final int numero;
    private final Visitante[] visitantes;
    private final Random random = new Random();
    private boolean viajeCompletado;

    public GomonDoble(int numero) {
        this.numero = numero;
        this.visitantes = new Visitante[2];
    }

    public void usarGomon(Visitante v, int i) {
        visitantes[i] = v;
        if (i == 0) {
            Tiempo.informar(v + " se sube a un gomon " + numero + " doble vacio");
            synchronized (this) {
                viajeCompletado = false;
            }
        } else {
            Tiempo.informar(v + " se sube al gomon doble " + numero + " con " + visitantes[0]);
        }
    }

    public synchronized boolean esperarFin() throws InterruptedException {
        while (!viajeCompletado && Tiempo.actividadesAbiertas()) {
            wait();
        }
        return viajeCompletado;
    }

    public void viajarEnGomon() throws InterruptedException {
        Tiempo.informar(visitantes[0] + " y " + visitantes[1] + " bajan el rio en gomon" + numero);
        Tiempo.esperarMinutos(10 + random.nextInt(5));
    }

    public synchronized void dejarGomon() {
        Tiempo.informar(
                visitantes[0] + " y " + visitantes[1] + " llegan al final de la carrera y dejan el gomon " + numero);
        viajeCompletado = true;
        notify();
    }

    public synchronized void avisarCierre() {
        notify();
    }

}