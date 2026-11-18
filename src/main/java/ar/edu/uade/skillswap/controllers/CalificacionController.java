package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.aplicacion.GestionCalificacion;
import ar.edu.uade.skillswap.aplicacion.GestionPerfil;
import ar.edu.uade.skillswap.aplicacion.comandos.CalificarCmd;

/**
 * Controller adicional (la UI del kit todavía no tiene pantalla de calificación).
 * Queda listo para conectarse y está cubierto por los tests.
 */
public class CalificacionController {

    private final GestionCalificacion gestion;
    private final GestionPerfil sesion;

    public CalificacionController(GestionCalificacion gestion, GestionPerfil sesion) {
        this.gestion = gestion;
        this.sesion = sesion;
    }

    /** Devuelve el id de la calificación creada. */
    public String calificar(String userId, String contratacionId, int puntaje, String comentario) {
        return ManejadorErrores.ejecutar("calificar", () -> {
            sesion.validarSesion(userId);
            return gestion.calificar(new CalificarCmd(userId, contratacionId, puntaje, comentario)).getId();
        });
    }
}
