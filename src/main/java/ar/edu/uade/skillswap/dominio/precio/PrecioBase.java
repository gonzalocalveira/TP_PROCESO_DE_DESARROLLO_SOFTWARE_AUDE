package ar.edu.uade.skillswap.dominio.precio;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.Validar;

/**
 * PATRÓN DECORATOR — Componente concreto.
 * Es el objeto "núcleo" que se envuelve: el precio que fijó el autor de la publicación.
 */
public class PrecioBase implements CalculadorPrecio {

    private final SkillCoins monto;

    public PrecioBase(SkillCoins monto) {
        Validar.que(!monto.esNegativo(), "El precio base no puede ser negativo.");
        this.monto = monto;
    }

    @Override
    public SkillCoins calcular() {
        return monto;
    }

    @Override
    public String descripcion() {
        return "base " + monto.aEntero();
    }
}
