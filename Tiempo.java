public class Tiempo extends Thread {
    private int cantMinutos; // total acumulado desde el arranque de la simulación
    private final long msPorMinuto;
    private final int cantDias; // 0 = indefinido
    private boolean activo;

    public Tiempo(int horaInicial, int minutoInicial, long msPorMinuto, int cantDias) {
        this.cantMinutos = horaInicial * 60 + minutoInicial;
        this.msPorMinuto = msPorMinuto;
        this.cantDias = cantDias;
        activo = true;
    }

    @Override
    public void run() {
        int minutosPorDia = 24 * 60;
        int limite = (cantDias != 0) ? cantDias * minutosPorDia : Integer.MAX_VALUE;

        while (!isInterrupted() && cantMinutos < limite) {
            try {
                Thread.sleep(msPorMinuto);
            } catch (InterruptedException e) {
                break;
            }
            avanzarMinuto();
        }
        synchronized (this) {
            activo = false;
        }
        System.out.println("Simulacion terminada. ");
    }

    public synchronized boolean estaActivo() {
        return activo;
    }

    private synchronized void avanzarMinuto() {
        cantMinutos++;
    }

    public synchronized int getDia() {
        return cantMinutos / (24 * 60) + 1;
    } // día 0, 1, 2...

    public synchronized int getHora() {
        return (cantMinutos / 60) % 24;
    } // 0-23

    public synchronized int getMinuto() {
        return cantMinutos % 60;
    }

    public synchronized int getCantMinutos() {
        return cantMinutos;
    }

    public long convertirMinutos(int minutos) {
        return minutos * msPorMinuto;
    }

    public synchronized String imprimirTiempo() {
        return String.format("Día %d - %02d:%02d", getDia(), getHora(), getMinuto());
    }

    public void esperarMinutos(int minutos) throws InterruptedException {
        if (minutos <= 0) {
            System.out.println("Esperar Minutos con valor invalido: " + minutos);
            return;
        }
        Thread.sleep(convertirMinutos(minutos));
    }
}