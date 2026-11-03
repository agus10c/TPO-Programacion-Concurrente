import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Carrera_De_Gomones implements Actividad {

    private static final int CANT_BICICLETAS = 10;
    private static final int CAPACIDAD_TREN = 15;
    private static final int CANT_GOMONES_INDIVIDUALES = 8;
    private static final int CANT_GOMONES_DOBLES = 3;
    private static final int GOMONES_PARA_LARGADA = 6;

    private final Random random = new Random();

    private final Camioneta camioneta;

    private final Semaphore bicicletas;
    private final Semaphore tren;
    private final Semaphore gomonesIndividuales;
    private final Queue<GomonDoble> gomonesDoblesDisponibles = new LinkedList<>();
    private GomonDoble gomonDobleParaEmparejar;
    private Largada largadaActual = new Largada(GOMONES_PARA_LARGADA);

    public Carrera_De_Gomones() {
        this.camioneta = new Camioneta();
        camioneta.start();
        this.gomonDobleParaEmparejar = null;
        this.bicicletas = new Semaphore(CANT_BICICLETAS, true);
        this.tren = new Semaphore(CAPACIDAD_TREN, true);
        this.gomonesIndividuales = new Semaphore(CANT_GOMONES_INDIVIDUALES, true);
        for (int i = 1; i <= CANT_GOMONES_DOBLES; i++) {
            gomonesDoblesDisponibles.add(new GomonDoble(i));
        }
    }

    private void viajarEnBicicleta(Visitante v) throws InterruptedException {
        bicicletas.acquire();
        try {
            Tiempo.informar(v + " toma una bicicleta.");
            Tiempo.esperarMinutos(5 + random.nextInt(5));
            Tiempo.informar(v + " llega al inicio de la carrera en bicicleta.");
        } finally {
            bicicletas.release();
        }
    }

    private void viajarEnTren(Visitante v) throws InterruptedException {
        tren.acquire();
        try {
            Tiempo.informar(v + " sube al tren.");
            Tiempo.esperarMinutos(5 + random.nextInt(5));
            Tiempo.informar(v + " llega al inicio de la carrera en tren.");
        } finally {
            tren.release();
        }
    }

    public void notificarCierre() {

        synchronized (this) {
            if (gomonDobleParaEmparejar != null) {
                GomonDoble gomon = gomonDobleParaEmparejar;
                gomonDobleParaEmparejar = null;
                gomon.avisarCierre();
            }
            largadaActual.abortar();
            this.notifyAll();
        }

        camioneta.notificarCierre();
    }

    public boolean esperarLargada(Visitante v) throws InterruptedException {
        Largada largada;

        synchronized (this) {
            if (!Tiempo.actividadesAbiertas()) {
                return false;
            }

            largada = largadaActual;
        }

        largada.esperarLargada();

        synchronized (this) {
            if (largada.cambiarLargada()) {
                largada.cambiada();
                largadaActual = new Largada(GOMONES_PARA_LARGADA);
            }
        }

        return !largada.getAbortada();
    }

    @Override
    public void realizarActividad(Visitante v) throws InterruptedException {
        if (!Tiempo.actividadesAbiertas()) {
            return;
        }
        Tiempo.informar(v + " Va a hacer Carrera de gomones");
        if (random.nextBoolean()) {
            viajarEnBicicleta(v);
        } else {
            viajarEnTren(v);
        }

        int nroBolso = camioneta.guardarBolso(v);
        if (nroBolso == -1) {
            Tiempo.informar(v + " no pudo guardar su bolso. Las actividades ya cerraron");
            return;
        }

        boolean esDoble = random.nextBoolean();

        if (esDoble) {
            GomonDoble gomon = null;
            boolean emparejado = false;

            synchronized (this) {
                if (gomonDobleParaEmparejar == null) {
                    // Buscar un gomón disponible para ser el primer visitante.
                    while (gomonesDoblesDisponibles.isEmpty()
                            && Tiempo.actividadesAbiertas()) {
                        this.wait();
                    }

                    if (gomonDobleParaEmparejar != null) {
                        gomon = gomonDobleParaEmparejar;
                        gomon.usarGomon(v, 1);
                        gomonDobleParaEmparejar = null;
                        emparejado = true;
                    } else if (Tiempo.actividadesAbiertas()) {
                        gomon = gomonesDoblesDisponibles.poll();
                        gomon.usarGomon(v, 0);
                        gomonDobleParaEmparejar = gomon;
                    }
                } else if (Tiempo.actividadesAbiertas()) {
                    // Hay un gomón esperando al segundo visitante.
                    gomon = gomonDobleParaEmparejar;
                    gomon.usarGomon(v, 1);
                    gomonDobleParaEmparejar = null;
                    emparejado = true;
                }
            }

            if (gomon == null) {
                camioneta.retirarBolso(v, nroBolso);
                return;
            }

            if (!emparejado) {
                // caso del primer visitante que toma gomon doble vacio
                if (!gomon.esperarFin()) {
                    Tiempo.informar("Las actividades cerraron mientras " + v + " esperaba en gomon doble");
                }
                synchronized (this) {
                    gomonesDoblesDisponibles.add(gomon);
                    this.notifyAll();
                }
            } else {
                // caso de visitante que se empareja en gomon doble

                if (!esperarLargada(v)) {
                    gomon.avisarCierre();
                    camioneta.retirarBolso(v, nroBolso);
                    return;
                }
                gomon.viajarEnGomon();
                gomon.dejarGomon();
            }

        } else {
            gomonesIndividuales.acquire();
            if (!esperarLargada(v)) {
                camioneta.retirarBolso(v, nroBolso);
                gomonesIndividuales.release();
                return;
            }
            Tiempo.informar(v + " baja el rio en gomon individual");
            Tiempo.esperarMinutos(10 + random.nextInt(5));
            Tiempo.informar(v + " llega al final de la carrera y deja el gomon individual");
            gomonesIndividuales.release();

        }
        camioneta.retirarBolso(v, nroBolso);

    }

}