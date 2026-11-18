package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.puertos.FiltroBusqueda;
import ar.edu.uade.skillswap.aplicacion.puertos.Pagina;
import ar.edu.uade.skillswap.aplicacion.puertos.PublicacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.aplicacion.vistas.PublicacionDetalle;

import java.util.ArrayList;
import java.util.List;

/** Caso de uso: búsqueda paginada con filtros y orden. */
public class BusquedaPublicaciones {

    private final PublicacionRepository publicaciones;
    private final UnidadDeTrabajo tx;
    private final EnsambladorVistas vistas;

    public BusquedaPublicaciones(PublicacionRepository publicaciones, UnidadDeTrabajo tx, EnsambladorVistas vistas) {
        this.publicaciones = publicaciones;
        this.tx = tx;
        this.vistas = vistas;
    }

    public Pagina<PublicacionDetalle> buscar(FiltroBusqueda filtro) {
        return tx.ejecutar(() -> {
            var pagina = publicaciones.buscar(filtro);
            return new Pagina<>(vistas.publicaciones(pagina.contenido()), pagina.pagina(), pagina.tamanio(), pagina.total());
        });
    }

    public List<String> categorias() {
        List<String> todas = new ArrayList<>();
        todas.add(CatalogoCategorias.TODAS);
        todas.addAll(CatalogoCategorias.CATEGORIAS);
        return todas;
    }
}
