package ar.edu.uade.skillswap.dominio.contratacion.estado;

import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/**
 * PATRÓN STATE — Interfaz Estado (Contratación).
 *
 *   PENDIENTE --aceptar--> ACEPTADA --pagar--> CONFIRMADA --iniciar--> EN_CURSO --completar--> COMPLETADA
 *   PENDIENTE/ACEPTADA --rechazar--> RECHAZADA
 *   PENDIENTE/ACEPTADA/CONFIRMADA --cancelar--> CANCELADA
 *
 * Una acción inválida para el estado actual lanza TransicionInvalidaException.
 * Ejemplo: pagar en CONFIRMADA falla, y eso impide el doble pago.
 */
public interface EstadoContratacion {

    void aceptar(Contratacion c);

    void rechazar(Contratacion c, String motivo);

    void pagar(Contratacion c);

    void iniciar(Contratacion c);

    void completar(Contratacion c);

    void cancelar(Contratacion c, String motivo);

    String nombre();

    default boolean permiteReprogramar() {
        return false;
    }

    default boolean permiteCalificar() {
        return false;
    }

    /** Estados terminales: ya no admiten acciones. */
    default boolean esFinal() {
        return false;
    }

    /** Indica si el cliente ya pagó (define si una cancelación implica reintegro). */
    default boolean estaPagada() {
        return false;
    }

    static EstadoContratacion porNombre(String nombre) {
        return switch (nombre) {
            case "PENDIENTE" -> new Pendiente();
            case "ACEPTADA" -> new Aceptada();
            case "CONFIRMADA" -> new Confirmada();
            case "EN_CURSO" -> new EnCurso();
            case "COMPLETADA" -> new Completada();
            case "RECHAZADA" -> new Rechazada();
            case "CANCELADA" -> new Cancelada();
            default -> throw new IllegalArgumentException("Estado de contratación desconocido: " + nombre);
        };
    }
}
