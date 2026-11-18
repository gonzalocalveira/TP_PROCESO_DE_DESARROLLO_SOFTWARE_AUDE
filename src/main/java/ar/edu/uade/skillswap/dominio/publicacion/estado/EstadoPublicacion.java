package ar.edu.uade.skillswap.dominio.publicacion.estado;

import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

/**
 * PATRÓN STATE — Interfaz Estado (Publicación).
 * Cada estado concreto implementa TODAS las acciones: las válidas cambian el estado del contexto
 * y las inválidas lanzan TransicionInvalidaException. Así las reglas de transición quedan
 * encapsuladas en cada clase y no repartidas en ifs/switch por todo el sistema.
 *
 *   BORRADOR --publicar--> PUBLICADA --pausar--> PAUSADA --publicar--> PUBLICADA
 *   PUBLICADA/PAUSADA --cerrar--> CERRADA      cualquiera salvo CERRADA --cancelar--> CANCELADA
 */
public interface EstadoPublicacion {

    void publicar(Publicacion p);

    void pausar(Publicacion p);

    void cerrar(Publicacion p);

    void cancelar(Publicacion p, String motivo);

    String nombre();

    /** Solo una publicación PUBLICADA recibe solicitudes (regla del enunciado). */
    default boolean aceptaSolicitudes() {
        return false;
    }

    /** Reconstruye el objeto estado a partir del nombre guardado en la base. */
    static EstadoPublicacion porNombre(String nombre) {
        return switch (nombre) {
            case "BORRADOR" -> new Borrador();
            case "PUBLICADA" -> new Publicada();
            case "PAUSADA" -> new Pausada();
            case "CERRADA" -> new Cerrada();
            case "CANCELADA" -> new Cancelada();
            default -> throw new IllegalArgumentException("Estado de publicación desconocido: " + nombre);
        };
    }
}
