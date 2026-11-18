package ar.edu.uade.skillswap.infraestructura.persistencia;

import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.dominio.comun.ConflictoConcurrenciaException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.OptimisticLockException;
import org.hibernate.StaleStateException;

import java.util.function.Supplier;

/**
 * Implementación JPA del puerto UnidadDeTrabajo (inversión de dependencias: la aplicación
 * define la interfaz, la infraestructura la implementa).
 * Abre EntityManager + transacción, ejecuta el trabajo, hace commit o rollback.
 * Las llamadas anidadas se suman a la transacción ya abierta.
 * Traduce el fallo del bloqueo optimista (@Version) a ConflictoConcurrenciaException.
 */
public class JpaUnidadDeTrabajo implements UnidadDeTrabajo {

    private final EntityManagerFactory emf;

    public JpaUnidadDeTrabajo(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public <T> T ejecutar(Supplier<T> trabajo) {
        if (ContextoPersistencia.hayTransaccion()) {
            return trabajo.get();
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction t = em.getTransaction();
        ContextoPersistencia.abrir(em);
        try {
            t.begin();
            T resultado = trabajo.get();
            em.flush();
            t.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (t.isActive()) {
                t.rollback();
            }
            if (esConflictoDeVersion(e)) {
                throw new ConflictoConcurrenciaException(
                        "Otro usuario modificó estos datos al mismo tiempo (por ejemplo, tomó el último cupo). Volvé a intentarlo.", e);
            }
            throw e;
        } finally {
            ContextoPersistencia.cerrar();
            em.close();
        }
    }

    private static boolean esConflictoDeVersion(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof OptimisticLockException || t instanceof StaleStateException) {
                return true;
            }
        }
        return false;
    }
}
