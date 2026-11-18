package ar.edu.uade.skillswap.dominio.recomendacion;

import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

import java.util.List;

/**
 * PATRÓN STRATEGY — Estrategia (recomendaciones).
 * Familia de algoritmos intercambiables para ordenar las publicaciones candidatas.
 * El caso de uso RecomendarPublicaciones solo conoce esta interfaz: cambiar el criterio
 * (afinidad, popularidad, o uno nuevo) no obliga a modificar el caso de uso (Open/Closed).
 */
public interface RecomendacionStrategy {

    /** Devuelve las candidatas ordenadas de más a menos recomendable (no recorta). */
    List<Publicacion> recomendar(Perfil perfil, List<Publicacion> candidatas);

    String nombre();
}
