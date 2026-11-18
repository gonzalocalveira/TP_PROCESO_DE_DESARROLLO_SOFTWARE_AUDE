package ar.edu.uade.skillswap.dominio.evento;

/** Eventos relevantes del negocio. El "tipo" de notificación que ve la UI deriva de acá. */
public enum TipoEvento {
    SOLICITUD_RECIBIDA("RESERVA"),
    SOLICITUD_ACEPTADA("RESERVA"),
    SOLICITUD_RECHAZADA("RESERVA"),
    PAGO_CONFIRMADO("PAGO"),
    SESION_INICIADA("RESERVA"),
    CONTRATACION_COMPLETADA("RESERVA"),
    CONTRATACION_CANCELADA("RESERVA"),
    CONTRATACION_REPROGRAMADA("RESERVA"),
    CALIFICACION_RECIBIDA("REPUTACION"),
    PUBLICACION_ACTUALIZADA("PUBLICACION"),
    CREDITOS_ACREDITADOS("PAGO");

    private final String categoria;

    TipoEvento(String categoria) {
        this.categoria = categoria;
    }

    public String categoria() {
        return categoria;
    }
}
