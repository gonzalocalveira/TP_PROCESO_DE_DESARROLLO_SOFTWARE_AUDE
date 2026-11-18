package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

import java.time.LocalDateTime;

/** Constancia devuelta por el procesador de pagos (se guarda en cada movimiento de la wallet). */
public record Comprobante(String codigo, String userId, SkillCoins monto, LocalDateTime fecha) {
}
