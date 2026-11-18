package ar.edu.uade.skillswap.dominio.contratacion;

import ar.edu.uade.skillswap.dominio.comun.Ids;
import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.Validar;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Calificación de una contratación. Reglas del enunciado:
 *  - solo califican los participantes de una contratación COMPLETADA (por eso queda "verificada"),
 *  - nadie se califica a sí mismo,
 *  - una sola vez por participante (lo controla el caso de uso + restricción UNIQUE en la base).
 */
@Entity
@Table(name = "calificaciones", uniqueConstraints = @UniqueConstraint(
        name = "uk_calificacion_unica", columnNames = {"contratacion_id", "autor_id"}))
public class Calificacion {

    @Id
    @Column(length = 64)
    private String id;
    @Column(name = "contratacion_id", nullable = false, length = 64)
    private String contratacionId;
    @Column(name = "autor_id", nullable = false, length = 64)
    private String autorId;
    @Column(name = "calificado_id", nullable = false, length = 64)
    private String calificadoId;
    @Column(nullable = false)
    private int puntaje;
    @Column(length = 500)
    private String comentario;
    @Column(nullable = false)
    private boolean verificada;
    @Column(nullable = false)
    private LocalDateTime fecha;

    protected Calificacion() {
        // requerido por JPA
    }

    private Calificacion(Contratacion c, String autorId, String calificadoId, int puntaje, String comentario,
                         LocalDateTime fecha) {
        this.id = Ids.nuevo("cal");
        this.contratacionId = c.getId();
        this.autorId = autorId;
        this.calificadoId = calificadoId;
        this.puntaje = puntaje;
        this.comentario = comentario == null ? "" : comentario.strip();
        this.fecha = fecha;
    }

    public static Calificacion crear(Contratacion c, String autorId, int puntaje, String comentario, LocalDateTime ahora) {
        if (!c.esParticipante(autorId)) {
            throw new PermisoDenegadoException("Solo los participantes pueden calificar esta contratación.");
        }
        if (!c.getEstado().permiteCalificar()) {
            throw new ReglaNegocioException("Solo se puede calificar una contratación COMPLETADA.");
        }
        Validar.que(puntaje >= 1 && puntaje <= 5, "El puntaje debe estar entre 1 y 5.");
        String calificado = c.contraparteDe(autorId);
        if (calificado.equals(autorId)) {
            throw new ReglaNegocioException("No podés calificarte a vos mismo.");
        }
        Calificacion cal = new Calificacion(c, autorId, calificado, puntaje, comentario, ahora);
        cal.verificar();
        return cal;
    }

    /** Verificada = respaldada por una contratación completada (solo estas cuentan para la reputación). */
    public void verificar() {
        this.verificada = true;
    }

    public String getId() {
        return id;
    }

    public String getContratacionId() {
        return contratacionId;
    }

    public String getAutorId() {
        return autorId;
    }

    public String getCalificadoId() {
        return calificadoId;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public String getComentario() {
        return comentario;
    }

    public boolean esVerificada() {
        return verificada;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
