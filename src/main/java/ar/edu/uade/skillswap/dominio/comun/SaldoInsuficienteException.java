package ar.edu.uade.skillswap.dominio.comun;

public class SaldoInsuficienteException extends ReglaNegocioException {
    private static final long serialVersionUID = 1L;

    public SaldoInsuficienteException(SkillCoins saldo, SkillCoins requerido) {
        super("Saldo insuficiente: tenés " + saldo.aEntero() + " SkillCoins y se necesitan " + requerido.aEntero() + ".");
    }

    @Override
    public String codigo() {
        return "SALDO_INSUFICIENTE";
    }
}
