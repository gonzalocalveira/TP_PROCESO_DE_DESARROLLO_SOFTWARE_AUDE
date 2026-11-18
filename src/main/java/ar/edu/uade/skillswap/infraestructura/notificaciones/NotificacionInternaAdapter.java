package ar.edu.uade.skillswap.infraestructura.notificaciones;

import ar.edu.uade.skillswap.aplicacion.puertos.NotificacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.Notificador;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

/**
 * PATRÓN ADAPTER — Adapter del canal interno: adapta Notificador a la bandeja de la app
 * (repositorio de notificaciones). Es lo que ve el usuario en "Notificaciones".
 * Guarda en su propia transacción porque se ejecuta después del commit del caso de uso.
 */
public class NotificacionInternaAdapter implements Notificador {

    private final NotificacionRepository repositorio;
    private final UnidadDeTrabajo tx;

    public NotificacionInternaAdapter(NotificacionRepository repositorio, UnidadDeTrabajo tx) {
        this.repositorio = repositorio;
        this.tx = tx;
    }

    @Override
    public void enviar(Notificacion n) {
        tx.ejecutarAccion(() -> repositorio.guardar(n));
    }

    @Override
    public String canal() {
        return "interna";
    }
}
