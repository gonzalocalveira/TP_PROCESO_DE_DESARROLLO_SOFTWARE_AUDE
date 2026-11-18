package ar.edu.uade.skillswap.dominio.comun;

/** Raíz de las excepciones de negocio. Los controllers las traducen a un mensaje uniforme. */
public abstract class SkillSwapException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    protected SkillSwapException(String mensaje) {
        super(mensaje);
    }

    protected SkillSwapException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /** Código estable para logs y para la UI. */
    public abstract String codigo();
}
