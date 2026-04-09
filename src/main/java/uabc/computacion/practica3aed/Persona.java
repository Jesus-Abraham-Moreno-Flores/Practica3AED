package uabc.computacion.practica3aed;

public class Persona {
    private static int contadorGlobal = 0;
    private final int id;
    private final int rondaEntrada;
    private int rondaSalida;

    public Persona(int rondaEntrada){
        this.id = ++contadorGlobal;
        this.rondaEntrada = rondaEntrada;
        this.rondaSalida = -1;
    }

    public void registrarSalida(int ronda){
        this.rondaSalida = ronda;
    }

    public void registrar
}
