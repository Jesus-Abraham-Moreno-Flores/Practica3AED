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

    public int getTiempoEnSistema(){
        if(rondaSalida == -1) return -1;
        return rondaSalida - rondaEntrada;
    }

    public int getId(){ return id; }
    public int getRondaEntrada(){ return rondaEntrada; }
    public int getRondaSalida(){ return rondaSalida; }

    public static void reiniciarContador(){ contadorGlobal = 0; }
}
