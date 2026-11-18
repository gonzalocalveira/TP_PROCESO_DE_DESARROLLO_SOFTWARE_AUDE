package ar.edu.uade.skillswap.dominio.precio;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

/**
 * PATRÓN DECORATOR — Componente.
 * Interfaz común para el precio base y para todos los "decoradores" (promociones, comisiones).
 * Quien pide el precio (Publicacion, GestionContratacion) solo conoce esta interfaz y no sabe
 * cuántas capas de promociones/comisiones hay apiladas.
 */
public interface CalculadorPrecio {

    SkillCoins calcular();

    /** Detalle legible de cómo se armó el precio (útil para la UI y para depurar). */
    String descripcion();
}
