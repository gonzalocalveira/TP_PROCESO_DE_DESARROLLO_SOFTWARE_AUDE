package ar.edu.uade.skillswap.infraestructura.pagos;

import java.util.concurrent.atomic.AtomicLong;

/**
 * SIMULA la pasarela de pagos externa. PATRÓN ADAPTER — Adaptee.
 * Interfaz incompatible con la nuestra: trabaja en CENTAVOS (long), un monto negativo es un
 * pago hacia el usuario, no conoce usuarios y responde texto "OK:&lt;id&gt;" o "ERROR:&lt;motivo&gt;".
 */
public class SkillCoinsGatewaySDK {

    private final long limiteCentavos;
    private final AtomicLong secuencia = new AtomicLong(1000);

    public SkillCoinsGatewaySDK(long limiteCentavos) {
        this.limiteCentavos = limiteCentavos;
    }

    public String charge(long centavos) {
        if (centavos == 0) {
            return "ERROR:ZERO_AMOUNT";
        }
        if (Math.abs(centavos) > limiteCentavos) {
            return "ERROR:LIMIT_EXCEEDED";
        }
        return "OK:TX-" + secuencia.incrementAndGet();
    }
}
