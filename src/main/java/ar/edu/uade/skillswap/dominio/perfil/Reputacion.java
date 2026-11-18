package ar.edu.uade.skillswap.dominio.perfil;

import ar.edu.uade.skillswap.dominio.comun.Validar;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value Object inmutable: promedio de calificaciones verificadas y cantidad de reseñas.
 * Cada nueva calificación genera una nueva Reputacion (no se modifica la existente).
 */
@Embeddable
public class Reputacion {

    @Column(nullable = false)
    private double promedio;
    @Column(nullable = false)
    private int cantidad;

    protected Reputacion() {
        // requerido por JPA
    }

    public Reputacion(double promedio, int cantidad) {
        Validar.que(cantidad >= 0, "La cantidad de reseñas no puede ser negativa.");
        Validar.que(promedio >= 0 && promedio <= 5, "El promedio debe estar entre 0 y 5.");
        this.promedio = promedio;
        this.cantidad = cantidad;
    }

    public static Reputacion inicial() {
        return new Reputacion(0, 0);
    }

    public Reputacion conNueva(int puntaje) {
        double nuevoPromedio = (promedio * cantidad + puntaje) / (cantidad + 1);
        return new Reputacion(nuevoPromedio, cantidad + 1);
    }

    /** Promedio redondeado a un decimal, como se muestra en la UI. */
    public double valor() {
        return Math.round(promedio * 10) / 10.0;
    }

    public int getCantidad() {
        return cantidad;
    }
}
