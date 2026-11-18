package ar.edu.uade.skillswap.dominio.contratacion.estado;

import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/** PATRÓN STATE — Estado concreto PENDIENTE. Esperando que el proveedor acepte o rechace. */
public class Pendiente implements EstadoContratacion {

    @Override
    public void aceptar(Contratacion c) {
        c.cambiarEstado(new Aceptada());
    }

    @Override
    public void rechazar(Contratacion c, String motivo) {
        c.cambiarEstado(new Rechazada());
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
        c.cambiarEstado(new Cancelada());
    }

    @Override
    public String nombre() {
        return "PENDIENTE";
    }

    @Override
    public boolean permiteReprogramar() {
        return true;
    }
}
