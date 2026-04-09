package uabc.computacion.practica3aed;

public class ColaSimple<T> {
    private T[] cola;
    private int inicio;
    private int fin;
    private int tamanio;

    public ColaSimple(int capacidad) {
        cola = (T[]) new Object[capacidad];
        inicio = -1;
        fin = -1;
        tamanio = 0;
    }

    public void insertarDato(T dato) {
        if (tamanio == cola.length) {
            System.out.println("Desbordamiento");
            return;
        }
        if (estaVacia()) {
            inicio = 0;
            fin = 0;
        } else {
            fin++;
        }
        cola[fin] = dato;
        tamanio++;
    }

    public T eliminarDato() {
        if (estaVacia()) {
            System.out.println("Subdesbordamiento");
            return null;
        }
        T dato = cola[inicio];
        tamanio--;
        if (tamanio == 0) {
            inicio = -1;
            fin = -1;
        } else {
            inicio++;
        }
        return dato;
    }

    public T peek() {
        if (estaVacia()) return null;
        return cola[inicio];
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public int size() {
        return tamanio;
    }
}
