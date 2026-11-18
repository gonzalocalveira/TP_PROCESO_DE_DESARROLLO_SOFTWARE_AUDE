package ar.edu.uade.skillswap.dominio.comun;

public class PermisoDenegadoException extends SkillSwapException {
    private static final long serialVersionUID = 1L;

    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String codigo() {
        return "PERMISO_DENEGADO";
    }
}
