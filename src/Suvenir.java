class Suvenir {
    private final String nombre;
    private int stock;

    Suvenir(String nombre, int stock) {
        this.nombre = nombre;
        this.stock = stock;
    }

    public String getNombre() {
        return nombre;
    }

    public int getStock() {
        return stock;
    }

    public void decrementarStock() {
        stock--;
    }

}