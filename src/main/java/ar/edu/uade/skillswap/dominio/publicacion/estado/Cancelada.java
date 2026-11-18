package ar.edu.uade.skillswap.dominio.publicacion.estado;

import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

/** PATRÓN STATE — Estado concreto CANCELADA. Estado final. */
public class Cancelada implements EstadoPublicacion {

    @Override
    public void publicar(Publicacion p) {
        throw new TransicionInvalidaException(nombre(), "publicar");
    }

    @Override
    public void pausar(Publicacion p) {
        throw new TransicionInvalidaException(nombre(), "pausar");
    }

    @Override
    public void cerrar(Publicacion p) {
        throw new TransicionInvalidaException(nombre(), "cerrar");
    }

    @Override
    public void cancelar(Publicacion p, String motivo) {
        throw new TransicionInvalidaException(nombre(), "cancelar");
    }

    @Override
    public String nombre() {
        return "CANCELADA";
    }
}
