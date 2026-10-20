public class AdminCarrera extends Thread {

    private final Tiempo tiempo;
    private final Carrera_De_Gomones carrera;

    public AdminCarrera(Tiempo tiempo, Carrera_De_Gomones carrera) {
        super("AdminCarrera");
        this.tiempo = tiempo;
        this.carrera = carrera;
    }

    @Override
    public void run() {
        while (tiempo.estaActivo()) {
            carrera.habilitarCarrera();
        }
        System.out.println("[" + tiempo.imprimirTiempo() + "] AdminCarrera termina.");
    }

}
