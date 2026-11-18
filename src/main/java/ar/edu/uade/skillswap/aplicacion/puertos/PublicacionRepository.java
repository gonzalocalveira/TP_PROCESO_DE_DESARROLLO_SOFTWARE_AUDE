package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.comun.RecursoNoEncontradoException;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

import java.util.List;
import java.util.Optional;

public interface PublicacionRepository {

    void guardar(Publicacion publicacion);

    Optional<Publicacion> porId(String id);

    /** Búsqueda paginada sobre publicaciones PUBLICADAS de autores activos. */
    Pagina<Publicacion> buscar(FiltroBusqueda filtro);

    List<Publicacion> porAutor(String autorId);

    List<Publicacion> publicadas();

    default Publicacion obtener(String id) {
        return porId(id).orElseThrow(() -> new RecursoNoEncontradoException("Publicación", id));
    }
}
