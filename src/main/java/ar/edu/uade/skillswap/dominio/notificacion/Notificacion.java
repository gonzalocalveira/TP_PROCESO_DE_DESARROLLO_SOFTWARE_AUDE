package ar.edu.uade.skillswap.dominio.notificacion;

import ar.edu.uade.skillswap.dominio.comun.Ids;
import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Notificación dirigida a un usuario. Se genera a partir de un EventoDominio. */
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @Column(length = 64)
    private String id;
    @Column(name = "destinatario_id", nullable = false, length = 64)
    private String destinatarioId;
    @Column(nullable = false, length = 30)
    private String tipo;
    @Column(nullable = false, length = 150)
    private String titulo;
    @Column(length = 500)
    private String mensaje;
    @Column(nullable = false)
    private LocalDateTime fecha;
    @Column(nullable = false)
    private boolean leida;

    protected Notificacion() {
        // requerido por JPA
    }

    public Notificacion(String destinatarioId, String tipo, String titulo, String mensaje, LocalDateTime fecha) {
        this.id = Ids.nuevo("not");
        this.destinatarioId = destinatarioId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public void marcarLeida(String userId) {
        if (!destinatarioId.equals(userId)) {
            throw new PermisoDenegadoException("La notificación no te pertenece.");
        }
        leida = true;
    }

    public boolean estaLeida() {
        return leida;
    }

    public String getId() {
        return id;
    }

    public String getDestinatarioId() {
        return destinatarioId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
