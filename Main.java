public class Main {
    public static void main(String[] args) throws Exception {
        ParqueECO_PCS parque = new ParqueECO_PCS();
        Tiempo tiempo = new Tiempo(parque);
        AdminFaroMirador admin = new AdminFaroMirador(parque.getFaro());
        Generador_De_Visitantes generadorVisitantes = new Generador_De_Visitantes(parque);
        tiempo.start();
        admin.start();
        generadorVisitantes.start();
        tiempo.join();
        admin.join();
        generadorVisitantes.join();
    }
}