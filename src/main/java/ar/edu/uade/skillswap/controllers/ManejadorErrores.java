package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.dominio.comun.SkillSwapException;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manejo UNIFORME de errores en la capa de controllers:
 *  - errores de negocio (SkillSwapException) se propagan tal cual: ya traen un mensaje claro,
 *  - cualquier otra excepción se registra completa en el log y se reemplaza por un mensaje genérico
 *    (no se filtran detalles técnicos como SQL o stack traces a la UI).
 */
final class ManejadorErrores {

    private static final Logger LOG = Logger.getLogger(ManejadorErrores.class.getName());

    private ManejadorErrores() {
    }

    static <T> T ejecutar(String operacion, Supplier<T> accion) {
        try {
            return accion.get();
        } catch (SkillSwapException e) {
            LOG.log(Level.FINE, operacion + " rechazada: [" + e.codigo() + "] " + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Error inesperado en " + operacion, e);
            throw new ErrorInesperadoException(e);
        }
    }

    static void ejecutar(String operacion, Runnable accion) {
        ejecutar(operacion, () -> {
            accion.run();
            return null;
        });
    }
}
