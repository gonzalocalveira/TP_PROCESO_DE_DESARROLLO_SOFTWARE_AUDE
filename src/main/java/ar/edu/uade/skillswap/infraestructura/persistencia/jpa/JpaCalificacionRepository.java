package ar.edu.uade.skillswap.infraestructura.persistencia.jpa;

import ar.edu.uade.skillswap.aplicacion.puertos.CalificacionRepository;
import ar.edu.uade.skillswap.dominio.contratacion.Calificacion;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JpaCalificacionRepository extends RepositorioJpa<Calificacion> implements CalificacionRepository {

    public JpaCalificacionRepository() {
        super(Calificacion.class);
    }

    @Override
    public void guardar(Calificacion calificacion) {
        guardarEntidad(calificacion, calificacion.getId());
    }

    @Override
    public boolean existe(String contratacionId, String autorId) {
        return em().createQuery("select count(c) from Calificacion c where c.contratacionId = :c and c.autorId = :a", Long.class)
                .setParameter("c", contratacionId)
                .setParameter("a", autorId)
                .getSingleResult() > 0;
    }

    @Override
    public List<Calificacion> porCalificado(String perfilId) {
        return em().createQuery("select c from Calificacion c where c.calificadoId = :p order by c.fecha desc", Calificacion.class)
                .setParameter("p", perfilId)
                .getResultList();
    }
}
