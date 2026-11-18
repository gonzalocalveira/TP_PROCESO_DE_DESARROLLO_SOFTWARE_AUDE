package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.precio.CalculadorPrecio;
import ar.edu.uade.skillswap.dominio.precio.PromocionDecorator;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Promoción vigente de una publicación (se persiste embebida). Sabe "decorar" un precio. */
@Embeddable
public class Promocion {

    @Column(length = 120)
    private String descripcion;
    @Column(nullable = false)
    private double porcentaje;
    @Column(name = "monto_fijo", nullable = false, precision = 12, scale = 0)
    private SkillCoins montoFijo = SkillCoins.cero();

    protected Promocion() {
        // requerido por JPA
    }

    public Promocion(String descripcion, double porcentaje, SkillCoins montoFijo) {
        this.descripcion = descripcion;
        this.porcentaje = porcentaje;
        this.montoFijo = montoFijo;
    }

    /** PATRÓN DECORATOR: cada promoción envuelve al calculador anterior. */
    CalculadorPrecio decorar(CalculadorPrecio envuelto) {
        return new PromocionDecorator(envuelto, descripcion, porcentaje, montoFijo);
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public SkillCoins getMontoFijo() {
        return montoFijo;
    }
}
