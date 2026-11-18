package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.comun.RecursoNoEncontradoException;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

import java.util.List;
import java.util.Optional;

public interface ContratacionRepository {

    void guardar(Contratacion contratacion);

    Optional<Contratacion> porId(String id);

    /** Contrataciones donde el usuario es cliente o proveedor. */
    List<Contratacion> porUsuario(String userId);

    default Contratacion obtener(String id) {
        return porId(id).orElseThrow(() -> new RecursoNoEncontradoException("Contratación", id));
    }
}
