package uabc.computacion.practica3aed;

import java.util.ArrayList;

public class Control {
    public static final int NUM_ESTACIONES = 10;
    public static final int PERSONAS_INICIALES_POR_COLA = 4;
    public static final int MAX_RONDAS = 20;
    private static final int CAPACIDAD_COLA = 200;
    private int tiempoTotalAcumulado = 0;
    private int rondaActual;
    private int totalPersonasEntradas;
    private ColaSimple<Persona> personasSalidas;
    private final ArrayList<Estacion> estaciones;
    private boolean dadosTirados;

    public Control(){
        estaciones = new ArrayList<>();
        personasSalidas = new ColaSimple<>(500);
        rondaActual = 0;
        totalPersonasEntradas = 0;
        dadosTirados = false;
        inicializar();
    }

    public void inicializar(){
        Persona.reiniciarContador();
        //Se crean las 10 estaciones
        for(int i = 1; i <= NUM_ESTACIONES; i++){
            estaciones.add(new Estacion(i));
        }
        //Se cargan cuatro personas inicialmente en cada cola de cada estacion
        for(int i = 0; i < NUM_ESTACIONES; i++){
            for(int j = 0; j < PERSONAS_INICIALES_POR_COLA; j++){
                Persona persona = new Persona(0);
                estaciones.get(i).agregarPersona(persona);
            }
            totalPersonasEntradas += PERSONAS_INICIALES_POR_COLA;
        }
    }
    //Primero se tiran todos los dados
    public void tirarDados(){
        if(dadosTirados) return;
        for(Estacion estacion : estaciones){
            estacion.tirarDado();
        }
        dadosTirados = true;
    }
    //Como segundo paso o parte del juego, debemos mover a las personas y procesar lo que se tenia antes de este turno
    //Por eso cada estacion se procesa al comienzo y ya luego las transferimos
    public void moverPersonas(){
        if(!dadosTirados) return;
        rondaActual++;
        //Aqui se introduce a nuevas personas dependiendo del dado antes de procesarlo
        int dadoEstacion1 = estaciones.get(0).getUltimoDado();
        for(int i = 0; i < dadoEstacion1; i++){
            Persona nueva = new Persona(rondaActual);
            estaciones.get(0).agregarPersona(nueva);
            totalPersonasEntradas++;
        }
        //Guardamos los resultados antes de transferirlos
        ArrayList<ColaSimple<Persona>> resultados = new ArrayList<>();
        for(Estacion estacion : estaciones){
            resultados.add(estacion.procesarPersonas());
        }
        //Ahora lo que sale de la estacion en el ciclo va a la cola de la estacion + 1
        for(int i = 0; i < NUM_ESTACIONES - 1; i++){
            estaciones.get(i + 1).recibirPersonas(resultados.get(i));
        }
        //Lo que se salio de la ultima estacion sale del sistema por llamarlo de alguna manera
        ColaSimple<Persona> salidaFinal = resultados.get(NUM_ESTACIONES - 1);
        while(!salidaFinal.estaVacia()){
            Persona persona = salidaFinal.eliminarDato();
            persona.registrarSalida(rondaActual);
            acumularTiempo(persona.getTiempoEnSistema());
            personasSalidas.insertarDato(persona);
        }
        dadosTirados = false;
    }

    public int getRondaActual(){ return rondaActual; }
    public boolean isDadosTirados(){ return dadosTirados; }
    public boolean isJuegoTerminado(){ return rondaActual >= MAX_RONDAS; }
    public ArrayList<Estacion> getEstaciones(){ return estaciones; }
    public int getPersonasSalidas(){ return personasSalidas.size(); }

    public double getTiempoPromedioPaso(){
        if(personasSalidas.size() == 0) return 0;
        return (double) tiempoTotalAcumulado / personasSalidas.size();
    }

    public void acumularTiempo(int tiempo){
        tiempoTotalAcumulado += tiempo;
    }
}
