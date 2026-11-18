package ar.edu.uade.skillswap.dominio.precio;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.Validar;

/**
 * PATRÓN DECORATOR — Decorador concreto: aplica una promoción.
 * Puede ser porcentual (ej: 20% OFF) o de monto fijo (ej: cupón de 50 SkillCoins).
 * Varias promociones se acumulan envolviéndose entre sí.
 */
public class PromocionDecorator extends PrecioDecorator {

    private final String nombre;
    private final double porcentaje;
    private final SkillCoins montoFijo;

    public PromocionDecorator(CalculadorPrecio envuelto, String nombre, double porcentaje, SkillCoins montoFijo) {
        super(envuelto);
        Validar.que(porcentaje >= 0 && porcentaje <= 100, "El porcentaje de una promoción debe estar entre 0 y 100.");
        Validar.que(!montoFijo.esNegativo(), "El descuento fijo no puede ser negativo.");
        this.nombre = nombre;
        this.porcentaje = porcentaje;
        this.montoFijo = montoFijo;
    }

    public static PromocionDecorator porcentual(CalculadorPrecio envuelto, String nombre, double porcentaje) {
        return new PromocionDecorator(envuelto, nombre, porcentaje, SkillCoins.cero());
    }

    public static PromocionDecorator montoFijo(CalculadorPrecio envuelto, String nombre, SkillCoins monto) {
        return new PromocionDecorator(envuelto, nombre, 0, monto);
    }

    @Override
    protected SkillCoins ajustar(SkillCoins precio) {
        return precio.restar(precio.aplicarPorcentaje(porcentaje)).restar(montoFijo);
    }

    @Override
    protected String detalle() {
        return nombre + (porcentaje > 0 ? " -" + porcentaje + "%" : "") + (montoFijo.esPositivo() ? " -" + montoFijo.aEntero() : "");
    }
}
