package ar.edu.uade.skillswap.dominio.comun;

/** Se viola una regla del dominio (ej: el autor intenta contratar su propia publicación). */
public class ReglaNegocioException extends SkillSwapException {
    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String codigo() {
        return "REGLA_NEGOCIO";
    }
}
