package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.aplicacion.GestionNotificaciones;
import ar.edu.uade.skillswap.aplicacion.GestionPerfil;
import ar.edu.uade.skillswap.model.Models;

import java.util.List;

public class NotificacionControllerImpl implements UiControllers.NotificacionController {

    private final GestionNotificaciones gestion;
    private final GestionPerfil sesion;

    public NotificacionControllerImpl(GestionNotificaciones gestion, GestionPerfil sesion) {
        this.gestion = gestion;
        this.sesion = sesion;
    }

    @Override
    public List<Models.Notificacion> listarNotificaciones(String userId) {
        return ManejadorErrores.ejecutar("listarNotificaciones", () -> {
            sesion.validarSesion(userId);
            return gestion.listar(userId).stream().map(MapeadorDto::notificacion).toList();
        });
    }

    @Override
    public void marcarLeida(String userId, String notificacionId) {
        ManejadorErrores.ejecutar("marcarLeida", () -> {
            sesion.validarSesion(userId);
            gestion.marcarLeida(userId, notificacionId);
        });
    }

    @Override
    public void marcarTodasLeidas(String userId) {
        ManejadorErrores.ejecutar("marcarTodasLeidas", () -> {
            sesion.validarSesion(userId);
            gestion.marcarTodasLeidas(userId);
        });
    }
}
