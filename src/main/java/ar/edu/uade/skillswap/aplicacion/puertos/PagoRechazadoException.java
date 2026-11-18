package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;

/** El procesador de pagos rechazó la operación. */
public class PagoRechazadoException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    public PagoRechazadoException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String codigo() {
        return "PAGO_RECHAZADO";
    }
}
