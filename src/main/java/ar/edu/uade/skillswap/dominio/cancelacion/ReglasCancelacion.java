package ar.edu.uade.skillswap.dominio.cancelacion;

import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

import java.time.LocalDateTime;

/**
 * Elige QUÉ estrategia de cancelación aplicar según la anticipación (horas configurables).
 *  - si cancela el proveedor            -> ReintegroTotal (el cliente no tiene la culpa)
 *  - si faltan >= horasReintegroTotal   -> ReintegroTotal
 *  - si faltan >= horasReintegroParcial -> ReintegroParcial
 *  - si no                              -> SinReintegro
 * Los umbrales se leen de skillswap.properties: se cambian sin recompilar.
 */
public class ReglasCancelacion {

    private final long horasReintegroTotal;
    private final long horasReintegroParcial;
    private final double porcentajeParcial;

    public ReglasCancelacion(long horasReintegroTotal, long horasReintegroParcial, double porcentajeParcial) {
        if (horasReintegroParcial > horasReintegroTotal) {
            throw new IllegalArgumentException("El umbral parcial no puede superar al total.");
        }
        this.horasReintegroTotal = horasReintegroTotal;
        this.horasReintegroParcial = horasReintegroParcial;
        this.porcentajeParcial = porcentajeParcial;
    }

    public PoliticaCancelacion politicaPara(Contratacion c, String actor, LocalDateTime ahora) {
        if (c.esProveedor(actor)) {
            return new ReintegroTotal();
        }
        long horas = c.horasHastaSesion(ahora);
        if (horas >= horasReintegroTotal) {
            return new ReintegroTotal();
        }
        if (horas >= horasReintegroParcial) {
            return new ReintegroParcial(porcentajeParcial);
        }
        return new SinReintegro();
    }
}
