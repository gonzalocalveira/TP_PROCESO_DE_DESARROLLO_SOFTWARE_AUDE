package ar.edu.uade.skillswap.infraestructura.pagos;

import ar.edu.uade.skillswap.aplicacion.puertos.Comprobante;
import ar.edu.uade.skillswap.aplicacion.puertos.PagoRechazadoException;
import ar.edu.uade.skillswap.aplicacion.puertos.ProcesadorPagos;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

/**
 * PATRÓN ADAPTER — Adapter.
 * ProcesadorPagos (Target, en SkillCoins y con usuario) -> SkillCoinsGatewaySDK.charge(centavos) (Adaptee).
 * Convierte unidades, invierte el signo para los créditos y traduce "ERROR:..." a una excepción
 * de negocio con mensaje entendible.
 */
public class PasarelaSkillCoinsAdapter implements ProcesadorPagos {

    private final SkillCoinsGatewaySDK sdk;
    private final Clock reloj;

    public PasarelaSkillCoinsAdapter(SkillCoinsGatewaySDK sdk, Clock reloj) {
        this.sdk = sdk;
        this.reloj = reloj;
    }

    @Override
    public Comprobante debitar(String userId, SkillCoins monto) {
        return procesar(userId, monto, centavos(monto));
    }

    @Override
    public Comprobante acreditar(String userId, SkillCoins monto) {
        return procesar(userId, monto, -centavos(monto));
    }

    private Comprobante procesar(String userId, SkillCoins monto, long centavos) {
        String respuesta = sdk.charge(centavos);
        if (respuesta.startsWith("OK:")) {
            return new Comprobante(respuesta.substring(3), userId, monto, LocalDateTime.now(reloj));
        }
        String motivo = respuesta.startsWith("ERROR:") ? respuesta.substring(6) : respuesta;
        throw new PagoRechazadoException(switch (motivo) {
            case "LIMIT_EXCEEDED" -> "La pasarela rechazó el pago: supera el límite por operación.";
            case "ZERO_AMOUNT" -> "La pasarela rechazó un pago de monto cero.";
            default -> "La pasarela rechazó el pago (" + motivo + ").";
        });
    }

    private static long centavos(SkillCoins monto) {
        return monto.valor().multiply(BigDecimal.valueOf(100)).longValueExact();
    }
}
