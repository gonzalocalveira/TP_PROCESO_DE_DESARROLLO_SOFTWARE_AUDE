package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

import java.util.List;
import java.util.Optional;

public interface NotificacionRepository {

    void guardar(Notificacion notificacion);

    Optional<Notificacion> porId(String id);

    /** Más recientes primero. */
    List<Notificacion> porUsuario(String userId);
}
