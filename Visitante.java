import java.util.Random;
import java.util.List;
import java.util.ArrayList;

public class Visitante implements Runnable {

    private static final int PROB_IRSE = 10; // chance de retirarse en cada vuelta del loop en %
    private static int contadorVisitantes = 0;

    private final int id;
    private final ParqueECO_PCS parque;
    private final boolean esDeTour;
    private final Random random = new Random();
    private Colectivo colectivo;
    private boolean almorzo = false;
    private boolean merendo = false;

    public Visitante(ParqueECO_PCS parque) {
        this.id = incrementarId();
        this.parque = parque;
        this.esDeTour = false;
        this.colectivo = null;
    }

    // Constructor para TOUR
    public Visitante(ParqueECO_PCS parque, Colectivo colectivo) {
        this.id = incrementarId();
        this.parque = parque;
        this.colectivo = colectivo;
        this.esDeTour = true;
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
                colectivo.subirYEsperar(this);
            }
            if (!parque.ingresar(this)) {
                return; // no llegó a entrar (cerró el ingreso mientras esperaba el molinete)
            }
            vidaEnElParque();
            parque.salir(this);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[" + parque.getTiempo().imprimirTiempo() + "] Visitante " + id + " interrumpido.");
        }
    }

    private void vidaEnElParque() throws InterruptedException {
        while (parque.parqueAbierto()) {
            if (random.nextInt(100) < PROB_IRSE) {
                break; // el visitante decide irse del parque
            }

            TipoActividad elegida = elegirActividad();

            realizarActividad(elegida);

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

            case SNORKEL:
                parque.getSnorkel().realizarActividad(this);

            case FARO_MIRADOR:
               parque.getFaro().realizarActividad(this);

            case GOMONES:
                parque.getGomones().realizarActividad(this);

            case RESTAURANTE_ALMUERZO: {
                this.almorzo = parque.elegirRestaurante().almorzar(this);
                almorzo = true;
            }
            case RESTAURANTE_MERIENDA: {
                this.merendo = parque.elegirRestaurante().merendar(this);
                merendo = true;
            }
        }

    } 

}