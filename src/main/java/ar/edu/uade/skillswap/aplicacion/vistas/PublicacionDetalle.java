package ar.edu.uade.skillswap.aplicacion.vistas;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

/** Modelo de lectura: publicación + datos del autor + precio final que paga el cliente. */
public record PublicacionDetalle(Publicacion publicacion, String autorNombre, double reputacionAutor,
                                 SkillCoins precioFinal, String composicionPrecio) {
}
