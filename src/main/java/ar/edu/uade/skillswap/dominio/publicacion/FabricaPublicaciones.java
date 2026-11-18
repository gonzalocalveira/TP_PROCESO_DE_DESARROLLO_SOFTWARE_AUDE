package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;

import java.time.LocalDateTime;

/** Crea la subclase correcta según el tipo pedido (evita que los casos de uso conozcan las subclases). */
public final class FabricaPublicaciones {
    private FabricaPublicaciones() {
    }

    public static Publicacion crear(TipoPublicacion tipo, String autorId, String titulo, String descripcion,
                                    String categoria, Modalidad modalidad, int duracionMin, int cupo,
                                    SkillCoins precio, LocalDateTime ahora) {
        return switch (tipo) {
            case MENTORIA -> new OfertaMentoria(autorId, titulo, descripcion, categoria, modalidad, duracionMin, cupo, precio, ahora);
            case SERVICIO -> new OfertaServicio(autorId, titulo, descripcion, categoria, modalidad, duracionMin, cupo, precio, ahora);
            case DESAFIO -> new Desafio(autorId, titulo, descripcion, categoria, modalidad, duracionMin, cupo, precio, ahora);
        };
    }
}
