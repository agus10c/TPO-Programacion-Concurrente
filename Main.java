public class Main {
    public static void main(String[] args) throws Exception {
        ParqueECO_PCS parque;
        Tiempo tiempo = new Tiempo(8, 0, 80); // hora inicial: 8:00, 100 ms por minuto
        parque = new ParqueECO_PCS(tiempo);
        AdminFaroMirador admin = new AdminFaroMirador(tiempo, parque.getFaro());
        Camioneta camioneta = new Camioneta(tiempo, parque.getGomones());
        Generador_De_Visitantes generadorVisitantes = new Generador_De_Visitantes(parque);
        tiempo.start();
        admin.start();
        camioneta.start();
        generadorVisitantes.start();
        tiempo.esperarCierres();
        parque.cerrarParque();
        tiempo.join();
        admin.join();
        camioneta.join();
        generadorVisitantes.join();
    }
}