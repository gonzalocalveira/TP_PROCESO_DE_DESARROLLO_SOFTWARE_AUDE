package ar.edu.uade.skillswap.dominio.contratacion.estado;

import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/** PATRÓN STATE — Estado concreto COMPLETADA. Estado final; habilita la calificación de ambos participantes. */
public class Completada implements EstadoContratacion {

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
        throw new TransicionInvalidaException(nombre(), "completar");
    }

    @Override
    public void cancelar(Contratacion c, String motivo) {
        throw new TransicionInvalidaException(nombre(), "cancelar");
    }

    @Override
    public String nombre() {
        return "COMPLETADA";
    }

    @Override
    public boolean esFinal() {
        return true;
    }

    @Override
    public boolean permiteCalificar() {
        return true;
    }

    @Override
    public boolean estaPagada() {
        return true;
    }
}
