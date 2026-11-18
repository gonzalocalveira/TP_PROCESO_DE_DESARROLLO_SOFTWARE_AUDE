package ar.edu.uade.skillswap.dominio.cancelacion;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/** PATRÓN STRATEGY — Estrategia concreta: devuelve un porcentaje configurable (50% por defecto). */
public class ReintegroParcial implements PoliticaCancelacion {

    private final double porcentaje;

    public ReintegroParcial(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    @Override
    public SkillCoins calcularReintegro(Contratacion c) {
        return c.getTotal().aplicarPorcentaje(porcentaje);
    }

    @Override
    public String nombre() {
        return "Reintegro parcial (" + (int) porcentaje + "%)";
    }
}
