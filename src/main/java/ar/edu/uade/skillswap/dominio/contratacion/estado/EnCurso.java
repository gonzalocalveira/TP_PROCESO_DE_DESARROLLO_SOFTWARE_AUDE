package ar.edu.uade.skillswap.dominio.contratacion.estado;

import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/** PATRÓN STATE — Estado concreto EN_CURSO. La sesión se está dictando; ya no puede cancelarse. */
public class EnCurso implements EstadoContratacion {

    @Override
    public void aceptar(Contratacion c) {
        throw new TransicionInvalidaException(nombre(), "aceptar");
    }

    @Override
    public void rechazar(Contratacion c, String motivo) {
        throw new TransicionInvalidaException(nombre(), "rechazar");
    }

    @Override
    public void pagar(Contratacion c) {
        throw new TransicionInvalidaException(nombre(), "pagar");
    }

    @Override
    public void iniciar(Contratacion c) {
        throw new TransicionInvalidaException(nombre(), "iniciar");
    }

    @Override
    public void completar(Contratacion c) {
        c.cambiarEstado(new Completada());
    }

    @Override
    public void cancelar(Contratacion c, String motivo) {
        throw new TransicionInvalidaException(nombre(), "cancelar");
    }

    @Override
    public String nombre() {
        return "EN_CURSO";
    }

    @Override
    public boolean estaPagada() {
        return true;
    }
}
