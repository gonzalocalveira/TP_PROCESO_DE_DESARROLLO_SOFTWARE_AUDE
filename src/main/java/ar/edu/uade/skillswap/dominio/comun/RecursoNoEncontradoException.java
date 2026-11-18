package ar.edu.uade.skillswap.dominio.comun;

public class RecursoNoEncontradoException extends SkillSwapException {
    private static final long serialVersionUID = 1L;

    public RecursoNoEncontradoException(String recurso, String id) {
        super(recurso + " inexistente: " + id);
    }

    @Override
    public String codigo() {
        return "NO_ENCONTRADO";
    }
}
