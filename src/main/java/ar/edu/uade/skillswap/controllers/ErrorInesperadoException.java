package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.dominio.comun.SkillSwapException;

/** Error técnico no previsto, traducido a un mensaje apto para el usuario. */
public class ErrorInesperadoException extends SkillSwapException {
    private static final long serialVersionUID = 1L;

    public ErrorInesperadoException(Throwable causa) {
        super("Ocurrió un error inesperado. Intentá nuevamente en unos minutos.", causa);
    }

    @Override
    public String codigo() {
        return "ERROR_INESPERADO";
    }
}
