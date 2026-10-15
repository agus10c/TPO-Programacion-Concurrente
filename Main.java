public class Main {
    public static void main() throws Exception{
        
        Tiempo tiempo = new Tiempo(8, 0, 80, 1); // hora inicial: 8:00, 100 ms por minuto, 1 día
        ParqueECO_PCS parque = new ParqueECO_PCS(tiempo);
        Thread admin = new AdminFaroMirador(tiempo, parque.getFaro());
        Thread camioneta = new Camioneta(tiempo, parque.getGomones());
        Thread generadorVisitantes = new Generador_De_Visitantes(parque);
        tiempo.start();
        admin.start();
        camioneta.start();
        generadorVisitantes.start();

        try {
            tiempo.join();
            admin.join();
            generadorVisitantes.join();
            camioneta.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}