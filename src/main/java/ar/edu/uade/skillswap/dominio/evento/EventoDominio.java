package ar.edu.uade.skillswap.dominio.evento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Hecho que ya ocurrió en el dominio (ej: "se confirmó el pago de la contratación X").
 * Es inmutable. Lo emiten los casos de uso y lo reciben los observers suscriptos.
 * El payload lleva destinatarios y textos para que los observers no tengan que consultar nada.
 */
public final class EventoDominio {

    private final TipoEvento tipo;
    private final LocalDateTime fecha;
    private final String referenciaId;
    private final List<String> destinatarios;
    private final String titulo;
    private final String detalle;

    public EventoDominio(TipoEvento tipo, LocalDateTime fecha, String referenciaId, List<String> destinatarios,
                         String titulo, String detalle) {
        this.tipo = tipo;
        this.fecha = fecha;
        this.referenciaId = referenciaId;
        this.destinatarios = List.copyOf(destinatarios);
        this.titulo = titulo;
        this.detalle = detalle;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getReferenciaId() {
        return referenciaId;
    }

    public List<String> getDestinatarios() {
        return destinatarios;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDetalle() {
        return detalle;
    }

    public Map<String, Object> getPayload() {
        return Map.of("referenciaId", referenciaId, "destinatarios", destinatarios, "titulo", titulo, "detalle", detalle);
    }

    @Override
    public String toString() {
        return tipo + "[" + referenciaId + "] -> " + destinatarios;
    }
}
