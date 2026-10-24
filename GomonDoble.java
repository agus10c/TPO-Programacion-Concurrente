import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.Random;

public class GomonDoble {
    private final int numero;
    private final Visitante[] visitantes;
    private final CyclicBarrier finCarrera = new CyclicBarrier(2);
    private final Random random = new Random();

    public GomonDoble(int numero) {
        this.numero = numero;

        this.visitantes = new Visitante[2];
    }

    public void usarGomon(Visitante v, int i) {
        visitantes[i] = v;
        if (i == 0) {
            Tiempo.informar(v + " se sube a un gomon " + numero + " doble vacio");
        } else {
            Tiempo.informar( v + " se sube al gomon doble " + numero + " con " + visitantes[0]);
        }
    }

    public void esperarFin() throws InterruptedException, BrokenBarrierException {
        finCarrera.await();
    }

    public void viajarEnGomon() throws InterruptedException {
        Tiempo.informar(visitantes[0] + " y " + visitantes[1] + " bajan el rio en gomon" + numero);
        Tiempo.esperarMinutos(10 + random.nextInt(5));
    }

    public void dejarGomon() throws InterruptedException, BrokenBarrierException {
        Tiempo.informar(visitantes[0] + " y " + visitantes[1] + " llegan al final de la carrera y dejan el gomon " + numero);
        esperarFin();
    }

    public void avisarCierre() {
        finCarrera.reset();
    }

}