package ar.edu.uade.skillswap.infraestructura.persistencia.jpa;

import ar.edu.uade.skillswap.aplicacion.puertos.NotificacionRepository;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaNotificacionRepository extends RepositorioJpa<Notificacion> implements NotificacionRepository {

    public JpaNotificacionRepository() {
        super(Notificacion.class);
    }

    @Override
    public void guardar(Notificacion notificacion) {
        guardarEntidad(notificacion, notificacion.getId());
    }

    @Override
    public Optional<Notificacion> porId(String id) {
        return Optional.ofNullable(buscar(id));
    }

    @Override
    public List<Notificacion> porUsuario(String userId) {
        return em().createQuery("select n from Notificacion n where n.destinatarioId = :u order by n.fecha desc, n.id", Notificacion.class)
                .setParameter("u", userId)
                .getResultList();
    }
}
