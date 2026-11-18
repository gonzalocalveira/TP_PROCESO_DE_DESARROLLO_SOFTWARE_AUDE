package ar.edu.uade.skillswap.dominio.comun;

/** Pequeñas guardas reutilizables para validar invariantes. */
public final class Validar {
    private Validar() {
    }

    public static String texto(String valor, String campo, int max) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo '" + campo + "' es obligatorio.");
        }
        String limpio = valor.strip();
        if (limpio.length() > max) {
            throw new ReglaNegocioException("El campo '" + campo + "' admite hasta " + max + " caracteres.");
        }
        return limpio;
    }

    public static void que(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ReglaNegocioException(mensaje);
        }
    }
}
