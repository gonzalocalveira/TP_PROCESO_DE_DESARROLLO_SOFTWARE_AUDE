package ar.edu.uade.skillswap.infraestructura.persistencia;

import jakarta.persistence.EntityManager;

/** Guarda el EntityManager de la transacción en curso del hilo actual (lo usan los repositorios). */
public final class ContextoPersistencia {

    private static final ThreadLocal<EntityManager> ACTUAL = new ThreadLocal<>();

    private ContextoPersistencia() {
    }

    public static EntityManager actual() {
        EntityManager em = ACTUAL.get();
        if (em == null) {
            throw new IllegalStateException("No hay transacción activa: los repositorios se usan dentro de UnidadDeTrabajo.ejecutar(...)");
        }
        return em;
    }

    static boolean hayTransaccion() {
        return ACTUAL.get() != null;
    }

    static void abrir(EntityManager em) {
        ACTUAL.set(em);
    }

    static void cerrar() {
        ACTUAL.remove();
    }
}
