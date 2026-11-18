package ar.edu.uade.skillswap.dominio.contratacion;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Registro INMUTABLE de un cambio de estado (fecha, actor y motivo, como pide el enunciado).
 * No tiene setters y la Contratacion solo permite agregar entradas, nunca editarlas ni borrarlas.
 */
@Embeddable
public class HistorialEstado {

    @Column(nullable = false)
    private LocalDateTime fecha;
    @Column(nullable = false, length = 64)
    private String actor;
    @Column(length = 300)
    private String motivo;
    @Column(name = "estado_anterior", length = 20)
    private String estadoAnterior;
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private String estadoNuevo;

    protected HistorialEstado() {
        // requerido por JPA
    }

    public HistorialEstado(LocalDateTime fecha, String actor, String motivo, String estadoAnterior, String estadoNuevo) {
        this.fecha = fecha;
        this.actor = actor;
        this.motivo = motivo;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getActor() {
        return actor;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }
}
