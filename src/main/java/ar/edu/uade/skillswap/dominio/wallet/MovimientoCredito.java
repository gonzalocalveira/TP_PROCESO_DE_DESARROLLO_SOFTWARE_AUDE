package ar.edu.uade.skillswap.dominio.wallet;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

/** Movimiento inmutable de la wallet (débito o crédito) con el saldo resultante. */
@Embeddable
public class MovimientoCredito {

    @Column(nullable = false, length = 200)
    private String concepto;
    @Column(nullable = false, precision = 14, scale = 0)
    private SkillCoins monto;
    @Column(nullable = false)
    private boolean debito;
    @Column(nullable = false)
    private LocalDateTime fecha;
    @Column(name = "saldo_resultante", nullable = false, precision = 14, scale = 0)
    private SkillCoins saldoResultante;
    @Column(length = 80)
    private String comprobante;

    protected MovimientoCredito() {
        // requerido por JPA
    }

    MovimientoCredito(String concepto, SkillCoins monto, boolean debito, LocalDateTime fecha,
                      SkillCoins saldoResultante, String comprobante) {
        this.concepto = concepto;
        this.monto = monto;
        this.debito = debito;
        this.fecha = fecha;
        this.saldoResultante = saldoResultante;
        this.comprobante = comprobante;
    }

    /** Importe con signo: negativo si es débito. */
    public SkillCoins importeConSigno() {
        return debito ? SkillCoins.cero().restar(monto) : monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public SkillCoins getMonto() {
        return monto;
    }

    public boolean esDebito() {
        return debito;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public SkillCoins getSaldoResultante() {
        return saldoResultante;
    }

    public String getComprobante() {
        return comprobante;
    }
}
