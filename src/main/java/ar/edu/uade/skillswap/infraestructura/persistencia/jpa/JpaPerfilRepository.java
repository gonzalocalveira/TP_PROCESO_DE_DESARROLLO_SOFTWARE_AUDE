package ar.edu.uade.skillswap.infraestructura.persistencia.jpa;

import ar.edu.uade.skillswap.aplicacion.puertos.PerfilRepository;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Implementación JPA del puerto PerfilRepository (patrón Repository). */
@Repository
public class JpaPerfilRepository extends RepositorioJpa<Perfil> implements PerfilRepository {

    public JpaPerfilRepository() {
        super(Perfil.class);
    }

    @Override
    public void guardar(Perfil perfil) {
        guardarEntidad(perfil, perfil.getId());
    }

    @Override
    public Optional<Perfil> porId(String id) {
        return Optional.ofNullable(buscar(id));
    }

    @Override
    public List<Perfil> porIds(Collection<String> ids) {
        return em().createQuery("select p from Perfil p where p.id in :ids", Perfil.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    @Override
    public long contar() {
        return em().createQuery("select count(p) from Perfil p", Long.class).getSingleResult();
    }
}
