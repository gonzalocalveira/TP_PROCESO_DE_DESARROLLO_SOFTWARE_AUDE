package ar.edu.uade.skillswap.dominio.comun;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object que representa la moneda interna de la plataforma.
 * Es inmutable: toda operación devuelve una nueva instancia.
 * Los SkillCoins son enteros (no hay fracciones de crédito), por eso se redondea HALF_UP.
 */
public final class SkillCoins implements Comparable<SkillCoins> {

    private static final SkillCoins CERO = new SkillCoins(BigDecimal.ZERO);

    private final BigDecimal monto;

    private SkillCoins(BigDecimal monto) {
        this.monto = monto.setScale(0, RoundingMode.HALF_UP);
    }

    public static SkillCoins de(long monto) {
        return new SkillCoins(BigDecimal.valueOf(monto));
    }

    public static SkillCoins de(BigDecimal monto) {
        return new SkillCoins(Objects.requireNonNull(monto, "monto"));
    }

    public static SkillCoins cero() {
        return CERO;
    }

    public SkillCoins sumar(SkillCoins otro) {
        return new SkillCoins(monto.add(otro.monto));
    }

    public SkillCoins restar(SkillCoins otro) {
        return new SkillCoins(monto.subtract(otro.monto));
    }

    /** Devuelve el porcentaje indicado de este monto (ej: 10 -> 10%). */
    public SkillCoins aplicarPorcentaje(double porcentaje) {
        return new SkillCoins(monto.multiply(BigDecimal.valueOf(porcentaje))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
    }

    public SkillCoins noNegativo() {
        return esNegativo() ? CERO : this;
    }

    public boolean esNegativo() {
        return monto.signum() < 0;
    }

    public boolean esPositivo() {
        return monto.signum() > 0;
    }

    public boolean esMayorQue(SkillCoins otro) {
        return compareTo(otro) > 0;
    }

    public boolean esMenorQue(SkillCoins otro) {
        return compareTo(otro) < 0;
    }

    public BigDecimal valor() {
        return monto;
    }

    public int aEntero() {
        return monto.intValueExact();
    }

    @Override
    public int compareTo(SkillCoins o) {
        return monto.compareTo(o.monto);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SkillCoins s && monto.compareTo(s.monto) == 0;
    }

    @Override
    public int hashCode() {
        return monto.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return monto.toPlainString() + " SC";
    }
}
