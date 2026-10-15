import java.util.List;
import java.util.Random;

public class Camioneta extends Thread {

    private final Tiempo tiempo;
    private final Carrera_De_Gomones carrera;

    private final Random random = new Random();

    public Camioneta(Tiempo tiempo, Carrera_De_Gomones carrera) {
        super("Camioneta");
        this.tiempo = tiempo;
        this.carrera = carrera;
    }

    @Override
    public void run() {
        try {
            while (tiempo.estaActivo()) {
                if (carrera.cantidadBolsosPendientes() < 6) {
                    tiempo.esperarMinutos(5);
                }
                List<Integer> bolsos = carrera.retirarBolsosPendientes();
                if (bolsos == null)
                    break;
                System.out.println("[" + tiempo.imprimirTiempo() + "] "
                        + "Camioneta lleva " + bolsos.size() + " bolsos.");
                tiempo.esperarMinutos(5 + random.nextInt(3));
                for (int nro : bolsos) {
                    carrera.depositarBolso(nro);
                }
                System.out.println("[" + tiempo.imprimirTiempo() + "] "
                        + "La camioneta dejó los bolsos para que los retiren.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] Camioneta termina.");
    }
}
