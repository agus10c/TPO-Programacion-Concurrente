import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class Shop implements Actividad {

    private final Random random = new Random();
    private final Semaphore cajas;
    private final Suvenir[] suvenires = {
            new Suvenir("Remera ECO", 10),
            new Suvenir("Llavero ECO", 10),
            new Suvenir("Gorra ECO", 10)
    };

    public Shop() {
        cajas = new Semaphore(2, true);
    }

    public void comprar(Visitante visitante) throws InterruptedException {
        int suvenirElegido = elegirConStock(visitante);
        if (suvenirElegido < 0) {
            Tiempo.informar(visitante + " encuentra el shop agotado y se retira.");
            return;
        }
        cajas.acquire();
        Tiempo.informar(
                visitante + " paga en una de las cajas del shop (" + suvenires[suvenirElegido].getNombre() + ").");
        Tiempo.esperarMinutos(1 + random.nextInt(3));
        cajas.release();
        Tiempo.informar(visitante + " compro " + suvenires[suvenirElegido].getNombre());
    }

    private synchronized int elegirConStock(Visitante v) {
        List<Integer> disponibles = new ArrayList<>();
        for (int i = 0; i < suvenires.length; i++) {
            if (suvenires[i].getStock() > 0)
                disponibles.add(i);
        }
        if (disponibles.isEmpty())
            return -1;
        int suvenirElegido = disponibles.get(random.nextInt(disponibles.size()));
        suvenires[suvenirElegido].decrementarStock();
        Tiempo.informar(v + " toma un " + suvenires[suvenirElegido].getNombre() + " (quedan "
                + suvenires[suvenirElegido].getStock() + ").");
        return suvenirElegido;
    }

    @Override
    public void realizarActividad(Visitante visitante) throws InterruptedException {
        Tiempo.informar(visitante + " va a comprar al shop");
        comprar(visitante);
    }

}