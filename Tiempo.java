public class Tiempo extends Thread {
    private int cantMinutos; // total acumulado desde el arranque de la simulación
    private final long msPorMinuto;
    private boolean finalizar;
    private boolean activo;;

    public Tiempo(int horaInicial, int minutoInicial, long msPorMinuto) {
        this.cantMinutos = horaInicial * 60 + minutoInicial;
        this.msPorMinuto = msPorMinuto;
        finalizar = false;
        activo = true;
    }

    @Override
    public void run() {
        while (!finalizar) {
            try {
                Thread.sleep(msPorMinuto);
            } catch (InterruptedException e) {
                break;
            }
            avanzarMinuto();
            if (cantMinutos == ParqueECO_PCS.CIERRE_ACTIVIDADES) {
                synchronized (this) {
                    this.notify();
                }
            }
            if (cantMinutos == ParqueECO_PCS.CIERRE_PARQUE) {
                synchronized (this) {
                    this.notify();
                }
            }

        }
        synchronized (this) {
            activo = false;
        }

        System.out.println("Tiempo finalizado.");
    }

    public synchronized boolean estaActivo() {
        return activo;
    }

    public synchronized void finalizar() {
        finalizar = true;
    }

    private synchronized void avanzarMinuto() {
        cantMinutos++;
    }

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
        return String.format("%02d:%02d", getHora(), getMinuto());
    }

    public void esperarMinutos(int minutos) throws InterruptedException {
        if (minutos <= 0) {
            System.out.println("Esperar Minutos con valor invalido: " + minutos);
            return;
        }
        Thread.sleep(convertirMinutos(minutos));
    }

    public synchronized boolean actividadesAbiertas() {
        return cantMinutos >= ParqueECO_PCS.APERTURA && cantMinutos < ParqueECO_PCS.CIERRE_ACTIVIDADES;
    }

    public synchronized boolean parqueAbierto() {
        return ParqueECO_PCS.APERTURA < cantMinutos && cantMinutos < ParqueECO_PCS.CIERRE_PARQUE;
    }

    public void esperarCierres() throws InterruptedException {
        synchronized (this) {
            this.wait();
            this.wait();
        }
    }

}