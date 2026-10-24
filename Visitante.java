import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class Visitante implements Runnable {

    private static final int PROB_IRSE = 20; // chance de retirarse en cada vuelta del loop en %
    private static int contadorVisitantes = 0;

    private final Random random = new Random();

    private final int id;
    private final ParqueECO_PCS parque;
    private final boolean esDeTour;
    private Colectivo colectivo;
    private boolean almorzo;
    private boolean merendo;

    public Visitante(ParqueECO_PCS parque) {
        this.id = incrementarId();
        this.parque = parque;
        this.esDeTour = false;
        this.colectivo = null;
        this.almorzo = false;
        this.merendo = false;
    }

    // Constructor para TOUR
    public Visitante(ParqueECO_PCS parque, Colectivo colectivo) {
        this.id = incrementarId();
        this.parque = parque;
        this.colectivo = colectivo;
        this.esDeTour = true;
        this.almorzo = false;
        this.merendo = false;
    }

    private static synchronized int incrementarId() {
        return ++contadorVisitantes;
    }

    public int getId() {
        return id;
    }

    public boolean esDeTour() {
        return esDeTour;
    }

    @Override
    public String toString() {
        return "Visitante-" + id;
    }

    @Override
    public void run() {
        try {
            if (esDeTour) {
                if (!colectivo.subirYEsperar(this)) {
                    return;
                }
            }
            if (!parque.ingresar(this)) {
                return; // no llegó a entrar (cerró el ingreso mientras esperaba el molinete)
            }
            vidaEnElParque();
            parque.salir(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Tiempo.informar("Visitante " + id + " interrumpido.");
        }
    }

    private void vidaEnElParque() throws InterruptedException {
        while (Tiempo.parqueAbierto()) {
            if (random.nextInt(100) < PROB_IRSE) {
                break; // el visitante decide irse del parque
            }
            TipoActividad elegida = elegirActividad();
            realizarActividad(elegida);
            Tiempo.esperarMinutos(10);
        }

    }

    private TipoActividad elegirActividad() {
        List<TipoActividad> disponibles = new ArrayList<>();
        disponibles.add(TipoActividad.SHOP);
        disponibles.add(TipoActividad.SNORKEL);
        disponibles.add(TipoActividad.FARO_MIRADOR);
        disponibles.add(TipoActividad.GOMONES);
        if (!almorzo)
            disponibles.add(TipoActividad.RESTAURANTE_ALMUERZO);
        if (!merendo)
            disponibles.add(TipoActividad.RESTAURANTE_MERIENDA);
        return disponibles.get(random.nextInt(disponibles.size()));
    }

    private void realizarActividad(TipoActividad actividadElegida) throws InterruptedException {
        switch (actividadElegida) {
            case SHOP:
                parque.getShop().realizarActividad(this);
                break;

            case SNORKEL:
                parque.getSnorkel().realizarActividad(this);
                break;
            case FARO_MIRADOR:
                parque.getFaro().realizarActividad(this);
                break;
            case GOMONES:
                parque.getGomones().realizarActividad(this);
                break;
            case RESTAURANTE_ALMUERZO: {
                this.almorzo = parque.elegirRestaurante().almorzar(this);
                break;
            }
            case RESTAURANTE_MERIENDA: {
                this.merendo = parque.elegirRestaurante().merendar(this);
                break;
            }
        }

    }

}