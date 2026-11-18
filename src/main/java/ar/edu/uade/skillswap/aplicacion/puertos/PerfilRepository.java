package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.comun.RecursoNoEncontradoException;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Puerto de salida (Repository): el dominio/aplicación define QUÉ necesita; infraestructura decide CÓMO. */
public interface PerfilRepository {

    void guardar(Perfil perfil);

    Optional<Perfil> porId(String id);

    List<Perfil> porIds(Collection<String> ids);

    long contar();

    default Perfil obtener(String id) {
        return porId(id).orElseThrow(() -> new RecursoNoEncontradoException("Perfil", id));
    }
}
