package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.puertos.NotificacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.dominio.comun.RecursoNoEncontradoException;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

import java.util.List;

/** Caso de uso: centro de notificaciones del usuario. */
public class GestionNotificaciones {

    private final NotificacionRepository notificaciones;
    private final UnidadDeTrabajo tx;

    public GestionNotificaciones(NotificacionRepository notificaciones, UnidadDeTrabajo tx) {
        this.notificaciones = notificaciones;
        this.tx = tx;
    }

    public List<Notificacion> listar(String userId) {
        return tx.ejecutar(() -> notificaciones.porUsuario(userId));
    }

    public void marcarLeida(String userId, String notificacionId) {
        tx.ejecutarAccion(() -> {
            Notificacion n = notificaciones.porId(notificacionId)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Notificación", notificacionId));
            n.marcarLeida(userId);              // valida que sea del usuario
            notificaciones.guardar(n);
        });
    }

    public void marcarTodasLeidas(String userId) {
        tx.ejecutarAccion(() -> notificaciones.porUsuario(userId).stream()
                .filter(n -> !n.estaLeida())
                .forEach(n -> {
                    n.marcarLeida(userId);
                    notificaciones.guardar(n);
                }));
    }
}
