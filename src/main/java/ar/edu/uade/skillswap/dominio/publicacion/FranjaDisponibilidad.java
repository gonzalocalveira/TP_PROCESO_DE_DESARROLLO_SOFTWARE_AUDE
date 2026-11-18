package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.Ids;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.Validar;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Franja horaria ofrecida por el autor. Una franja ocupada no puede reservarse otra vez. */
@Entity
@Table(name = "franjas_disponibilidad")
public class FranjaDisponibilidad {

    @Id
    @Column(length = 64)
    private String id;
    @Column(nullable = false)
    private LocalDateTime inicio;
    @Column(nullable = false)
    private LocalDateTime fin;
    @Column(nullable = false)
    private boolean ocupada;

    protected FranjaDisponibilidad() {
        // requerido por JPA
    }

    public FranjaDisponibilidad(LocalDateTime inicio, LocalDateTime fin) {
        Validar.que(inicio != null && fin != null, "La franja necesita inicio y fin.");
        Validar.que(fin.isAfter(inicio), "El fin de la franja debe ser posterior al inicio.");
        this.id = Ids.nuevo("fr");
        this.inicio = inicio;
        this.fin = fin;
    }

    public boolean estaDisponible(LocalDateTime ahora) {
        return !ocupada && inicio.isAfter(ahora);
    }

    void ocupar(LocalDateTime ahora) {
        if (ocupada) {
            throw new ReglaNegocioException("La franja " + inicio + " ya está ocupada.");
        }
        if (!inicio.isAfter(ahora)) {
            throw new ReglaNegocioException("No se puede reservar una franja que ya comenzó.");
        }
        ocupada = true;
    }

    void liberar() {
        ocupada = false;
    }

    public boolean seSuperponeCon(FranjaDisponibilidad otra) {
        return inicio.isBefore(otra.fin) && otra.inicio.isBefore(fin);
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public boolean isOcupada() {
        return ocupada;
    }
}
