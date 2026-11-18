package ar.edu.uade.skillswap.dominio.precio;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

import java.util.Objects;

/**
 * PATRÓN DECORATOR — Decorador abstracto.
 * Implementa la MISMA interfaz que el componente y además contiene uno (composición):
 * así los decoradores se pueden apilar en cualquier orden y cantidad.
 *
 * Regla 5 del enunciado: "las promociones se acumulan pero el total nunca es negativo".
 * Esa invariante se garantiza ACÁ, en un único lugar: calcular() es final y siempre
 * recorta a cero el resultado de cada capa, por más decoradores que se agreguen.
 */
public abstract class PrecioDecorator implements CalculadorPrecio {

    protected final CalculadorPrecio envuelto;

    protected PrecioDecorator(CalculadorPrecio envuelto) {
        this.envuelto = Objects.requireNonNull(envuelto, "envuelto");
    }

    @Override
    public final SkillCoins calcular() {
        // 1) delega en el objeto envuelto, 2) agrega su comportamiento, 3) protege la invariante
        return ajustar(envuelto.calcular()).noNegativo();
    }

    @Override
    public String descripcion() {
        return envuelto.descripcion() + " -> " + detalle();
    }

    /** Comportamiento agregado por cada decorador concreto. */
    protected abstract SkillCoins ajustar(SkillCoins precioEnvuelto);

    protected abstract String detalle();
}
