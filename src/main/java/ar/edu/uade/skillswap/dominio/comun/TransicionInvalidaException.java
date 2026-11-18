package ar.edu.uade.skillswap.dominio.comun;

/** La acción no está permitida en el estado actual (la lanzan los objetos State). */
public class TransicionInvalidaException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    public TransicionInvalidaException(String estadoActual, String accion) {
        super("No se puede " + accion + " cuando el estado es " + estadoActual + ".");
    }

    @Override
    public String codigo() {
        return "TRANSICION_INVALIDA";
    }
}
