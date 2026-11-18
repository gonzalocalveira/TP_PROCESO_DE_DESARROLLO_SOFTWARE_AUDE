package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

/**
 * PATRÓN ADAPTER — Target.
 * Operaciones de pago que usa la aplicación. PasarelaSkillCoinsAdapter las traduce a la
 * pasarela externa (que trabaja en centavos y devuelve códigos de texto).
 */
public interface ProcesadorPagos {

    Comprobante debitar(String userId, SkillCoins monto);

    Comprobante acreditar(String userId, SkillCoins monto);
}
