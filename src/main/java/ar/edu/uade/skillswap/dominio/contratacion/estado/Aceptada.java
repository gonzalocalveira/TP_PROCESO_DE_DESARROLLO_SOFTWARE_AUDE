package ar.edu.uade.skillswap.dominio.contratacion.estado;

import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/** PATRÓN STATE — Estado concreto ACEPTADA. Aceptada por el proveedor; falta el pago del cliente. */
public class Aceptada implements EstadoContratacion {

    @Override
    public void aceptar(Contratacion c) {
        throw new TransicionInvalidaException(nombre(), "aceptar");
    }

    @Override
    public void rechazar(Contratacion c, String motivo) {
        c.cambiarEstado(new Rechazada());
    }

    @Override
    public void pagar(Contratacion c) {
        c.cambiarEstado(new Confirmada());
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
        return "ACEPTADA";
    }

    @Override
    public boolean permiteReprogramar() {
        return true;
    }
}
