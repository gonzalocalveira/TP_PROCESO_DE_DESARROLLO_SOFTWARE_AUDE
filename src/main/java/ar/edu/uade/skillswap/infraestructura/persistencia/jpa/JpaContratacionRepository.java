package ar.edu.uade.skillswap.infraestructura.persistencia.jpa;

import ar.edu.uade.skillswap.aplicacion.puertos.ContratacionRepository;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaContratacionRepository extends RepositorioJpa<Contratacion> implements ContratacionRepository {

    public JpaContratacionRepository() {
        super(Contratacion.class);
    }

    @Override
    public void guardar(Contratacion contratacion) {
        guardarEntidad(contratacion, contratacion.getId());
    }

    @Override
    public Optional<Contratacion> porId(String id) {
        return Optional.ofNullable(buscar(id));
    }

    @Override
    public List<Contratacion> porUsuario(String userId) {
        return em().createQuery("select c from Contratacion c where c.clienteId = :u or c.proveedorId = :u", Contratacion.class)
                .setParameter("u", userId)
                .getResultList();
    }
}
