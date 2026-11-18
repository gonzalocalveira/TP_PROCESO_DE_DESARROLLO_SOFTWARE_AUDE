package ar.edu.uade.skillswap.dominio.cancelacion;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/**
 * PATRÓN STRATEGY — Estrategia (política de cancelación).
 * Encapsula "cuánto se le devuelve al cliente". Las tres variantes del enunciado
 * (100% / 50% / 0%) son clases intercambiables; el caso de uso no tiene ifs de porcentajes.
 */
public interface PoliticaCancelacion {

    SkillCoins calcularReintegro(Contratacion c);

    String nombre();
}
