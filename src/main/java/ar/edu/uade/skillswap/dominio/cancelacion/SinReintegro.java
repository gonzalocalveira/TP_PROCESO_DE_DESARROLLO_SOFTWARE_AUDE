package ar.edu.uade.skillswap.dominio.cancelacion;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/** PATRÓN STRATEGY — Estrategia concreta: cancelación sobre la hora, no hay devolución. */
public class SinReintegro implements PoliticaCancelacion {

    @Override
    public SkillCoins calcularReintegro(Contratacion c) {
        return SkillCoins.cero();
    }

    @Override
    public String nombre() {
        return "Sin reintegro (0%)";
    }
}
