package ar.edu.uade.skillswap.dominio.publicacion.estado;

import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

/** PATRÓN STATE — Estado concreto PAUSADA. Oculta temporalmente; puede reanudarse. */
public class Pausada implements EstadoPublicacion {

    @Override
    public void publicar(Publicacion p) {
        p.validarParaPublicar();
        p.cambiarEstado(new Publicada()); // reanudar
    }

    @Override
    public void pausar(Publicacion p) {
        throw new TransicionInvalidaException(nombre(), "pausar");
    }

    @Override
    public void cerrar(Publicacion p) {
        p.cambiarEstado(new Cerrada());
    }

    @Override
    public void cancelar(Publicacion p, String motivo) {
        p.cambiarEstado(new Cancelada());
    }

    @Override
    public String nombre() {
        return "PAUSADA";
    }
}
