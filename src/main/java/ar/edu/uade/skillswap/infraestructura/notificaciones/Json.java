package ar.edu.uade.skillswap.infraestructura.notificaciones;

/** Escape mínimo para armar los JSON que piden los SDKs externos. */
final class Json {
    private Json() {
    }

    static String texto(String s) {
        if (s == null) {
            return "null";
        }
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
