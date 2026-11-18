package ar.edu.uade.skillswap.infraestructura.persistencia.jpa;

import ar.edu.uade.skillswap.infraestructura.persistencia.ContextoPersistencia;
import jakarta.persistence.EntityManager;

/** Base común de los repositorios JPA: toma el EntityManager de la transacción en curso. */
abstract class RepositorioJpa<T> {

    private final Class<T> tipo;

    protected RepositorioJpa(Class<T> tipo) {
        this.tipo = tipo;
    }

    protected EntityManager em() {
        return ContextoPersistencia.actual();
    }

    /** Las entidades cargadas en la transacción se actualizan solas (dirty checking); las nuevas se persisten. */
    protected void guardarEntidad(T entidad, Object id) {
        EntityManager em = em();
        if (em.contains(entidad)) {
            return;
        }
        if (id != null && em.find(tipo, id) != null) {
            em.merge(entidad);
        } else {
            em.persist(entidad);
        }
    }

    protected T buscar(Object id) {
        return em().find(tipo, id);
    }
}
