package ar.edu.uade.skillswap.dominio.publicacion.estado;

import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

/** PATRÓN STATE — Estado concreto PUBLICADA. Es el único estado visible en búsquedas y que admite solicitudes. */
public class Publicada implements EstadoPublicacion {

    @Override
    public void publicar(Publicacion p) {
        throw new TransicionInvalidaException(nombre(), "publicar");
    }

    @Override
    public void pausar(Publicacion p) {
        p.cambiarEstado(new Pausada());
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
        return "PUBLICADA";
    }

    @Override
    public boolean aceptaSolicitudes() {
        return true;
    }
}
