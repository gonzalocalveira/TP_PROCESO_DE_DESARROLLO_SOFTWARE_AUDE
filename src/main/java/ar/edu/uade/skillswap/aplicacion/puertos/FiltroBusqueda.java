package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;

/**
 * Criterios de búsqueda + paginación. Valores nulos = sin filtro.
 * precioMaximo se compara contra el precio base publicado.
 */
public record FiltroBusqueda(String texto, String categoria, Modalidad modalidad, Integer precioMaximo,
                             OrdenBusqueda orden, int pagina, int tamanio) {

    public static final int TAMANIO_MAXIMO = 100;

    public FiltroBusqueda {
        if (pagina < 0) {
            throw new ReglaNegocioException("La página no puede ser negativa.");
        }
        if (tamanio < 1 || tamanio > TAMANIO_MAXIMO) {
            throw new ReglaNegocioException("El tamaño de página debe estar entre 1 y " + TAMANIO_MAXIMO + ".");
        }
        texto = texto == null || texto.isBlank() ? null : texto.strip();
        categoria = categoria == null || categoria.isBlank() || "Todas".equalsIgnoreCase(categoria) ? null : categoria;
        precioMaximo = precioMaximo == null || precioMaximo <= 0 ? null : precioMaximo;
        orden = orden == null ? OrdenBusqueda.RELEVANCIA : orden;
    }

    public static FiltroBusqueda todo(int tamanio) {
        return new FiltroBusqueda(null, null, null, null, OrdenBusqueda.RELEVANCIA, 0, tamanio);
    }
}
