package ar.edu.uade.skillswap.dominio.precio;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.Validar;

/**
 * PATRÓN DECORATOR — Decorador concreto: agrega la comisión de la plataforma.
 * Se aplica sobre el precio ya promocionado (la comisión se cobra sobre lo que realmente se paga).
 */
public class ComisionDecorator extends PrecioDecorator {

    private final double porcentaje;

    public ComisionDecorator(CalculadorPrecio envuelto, double porcentaje) {
        super(envuelto);
        Validar.que(porcentaje >= 0 && porcentaje <= 50, "La comisión debe estar entre 0% y 50%.");
        this.porcentaje = porcentaje;
    }

    /** Parte del total que corresponde a la plataforma. */
    public SkillCoins comision() {
        return calcular().restar(envuelto.calcular());
    }

    @Override
    protected SkillCoins ajustar(SkillCoins precio) {
        return precio.sumar(precio.aplicarPorcentaje(porcentaje));
    }

    @Override
    protected String detalle() {
        return "comisión +" + porcentaje + "%";
    }
}
