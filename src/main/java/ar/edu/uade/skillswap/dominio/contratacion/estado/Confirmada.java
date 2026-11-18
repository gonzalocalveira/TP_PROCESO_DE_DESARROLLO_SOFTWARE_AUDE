package ar.edu.uade.skillswap.dominio.contratacion.estado;

import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/** PATRÓN STATE — Estado concreto CONFIRMADA. Pagada: los SkillCoins ya se transfirieron. Pagar de nuevo es inválido (evita doble cobro). */
public class Confirmada implements EstadoContratacion {

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
        throw new TransicionInvalidaException(nombre(), "pagar otra vez");
    }

    @Override
    public void iniciar(Contratacion c) {
        c.cambiarEstado(new EnCurso());
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
        return "CONFIRMADA";
    }

    @Override
    public boolean permiteReprogramar() {
        return true;
    }

    @Override
    public boolean estaPagada() {
        return true;
    }
}
