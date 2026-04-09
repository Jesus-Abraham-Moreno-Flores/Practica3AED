package uabc.computacion.practica3aed;

import java.util.Random;

public class Estacion {
    private final int numero;
    private ColaSimple<Persona> cola;
    private int ultimoDado;
    private int personasMovidas;
    private static final Random random = new Random();
    private static final int CAPACIDAD_COLA = 200;

    public Estacion(int numero){
        this.numero = numero;
        this.cola = new ColaSimple<>(CAPACIDAD_COLA);
        this.ultimoDado = 0;
        this.personasMovidas = 0;
    }

    public int tirarDado(){
        ultimoDado = random.nextInt(6) + 1;
        return ultimoDado;
    }

    //Devuelve a las personas procesadas para pasarlas a la estacion que sigue
    public ColaSimple<Persona> procesarPersonas(){
        ColaSimple<Persona> procesadas = new ColaSimple<>(CAPACIDAD_COLA);
        int capacidad = ultimoDado;
        int movidas = 0;

        while(movidas < capacidad && !cola.estaVacia()){
            procesadas.insertarDato(cola.eliminarDato());
            movidas++;
        }
        personasMovidas = movidas;
        return procesadas;
    }

    //Recibe a las personas de la estacion anterior a la cola
    public void recibirPersonas(ColaSimple<Persona> entrantes){
        while(!entrantes.estaVacia()){
            cola.insertarDato(entrantes.eliminarDato());
        }
    }

    public void agregarPersona(Persona p){
        cola.insertarDato(p);
    }

    public int getNumero(){ return numero; }
    public int getUltimoDado(){ return ultimoDado; }
    public int getPersonasEnCola(){ return cola.size(); }
    public int getPersonasMovidas(){ return personasMovidas; }
}
