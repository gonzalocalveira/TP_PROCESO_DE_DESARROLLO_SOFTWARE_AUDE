package ar.edu.uade.skillswap.dominio.cancelacion;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/** PATRÓN STRATEGY — Estrategia concreta: devuelve el 100% (cancelación anticipada o culpa del proveedor). */
public class ReintegroTotal implements PoliticaCancelacion {

    @Override
    public SkillCoins calcularReintegro(Contratacion c) {
        return c.getTotal();
    }

    @Override
    public String nombre() {
        return "Reintegro total (100%)";
    }
}
