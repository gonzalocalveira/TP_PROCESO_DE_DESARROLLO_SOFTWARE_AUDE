package ar.edu.uade.skillswap.dominio.publicacion.estado;

import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

/** PATRÓN STATE — Estado concreto BORRADOR. Se puede editar; publicar exige datos completos. */
public class Borrador implements EstadoPublicacion {

    @Override
    public void publicar(Publicacion p) {
        p.validarParaPublicar();
        p.cambiarEstado(new Publicada());
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
        p.cambiarEstado(new Cancelada());
    }

    @Override
    public String nombre() {
        return "BORRADOR";
    }
}
