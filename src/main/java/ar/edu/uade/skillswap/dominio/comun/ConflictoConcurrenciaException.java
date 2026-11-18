package ar.edu.uade.skillswap.dominio.comun;

/** Dos usuarios modificaron el mismo recurso a la vez (bloqueo optimista con @Version). */
public class ConflictoConcurrenciaException extends SkillSwapException {
    private static final long serialVersionUID = 1L;

    public ConflictoConcurrenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    @Override
    public String codigo() {
        return "CONFLICTO_CONCURRENCIA";
    }
}
