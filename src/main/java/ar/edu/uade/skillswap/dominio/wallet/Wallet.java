package ar.edu.uade.skillswap.dominio.wallet;

import ar.edu.uade.skillswap.dominio.comun.SaldoInsuficienteException;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.Validar;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Billetera de SkillCoins de un perfil. El saldo nunca queda negativo.
 * La versión (@Version en el mapeo) evita que dos operaciones simultáneas pisen el saldo.
 */
@Entity
@Table(name = "wallets")
public class Wallet {

    public static final String PLATAFORMA = "plataforma";

    @Id
    @Column(name = "perfil_id", length = 64)
    private String perfilId;
    @Column(nullable = false, precision = 14, scale = 0)
    private SkillCoins saldo = SkillCoins.cero();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "wallet_movimientos", joinColumns = @JoinColumn(name = "wallet_id"))
    @OrderColumn(name = "orden")
    private List<MovimientoCredito> movimientos = new ArrayList<>();
    @Version
    private long version;

    protected Wallet() {
        // requerido por JPA
    }

    public Wallet(String perfilId) {
        this.perfilId = Validar.texto(perfilId, "perfil", 64);
    }

    public boolean puedeDebitar(SkillCoins monto) {
        return !saldo.esMenorQue(monto);
    }

    public void debitar(SkillCoins monto, String concepto, String comprobante, LocalDateTime fecha) {
        Validar.que(monto.esPositivo(), "El monto a debitar debe ser positivo.");
        if (!puedeDebitar(monto)) {
            throw new SaldoInsuficienteException(saldo, monto);
        }
        saldo = saldo.restar(monto);
        movimientos.add(new MovimientoCredito(concepto, monto, true, fecha, saldo, comprobante));
    }

    public void acreditar(SkillCoins monto, String concepto, String comprobante, LocalDateTime fecha) {
        Validar.que(monto.esPositivo(), "El monto a acreditar debe ser positivo.");
        saldo = saldo.sumar(monto);
        movimientos.add(new MovimientoCredito(concepto, monto, false, fecha, saldo, comprobante));
    }

    public String getPerfilId() {
        return perfilId;
    }

    public SkillCoins getSaldo() {
        return saldo;
    }

    public List<MovimientoCredito> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }
}
