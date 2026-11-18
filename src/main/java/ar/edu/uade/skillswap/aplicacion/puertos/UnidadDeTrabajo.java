package ar.edu.uade.skillswap.aplicacion.puertos;

import java.util.function.Supplier;

/**
 * Puerto de salida: delimita una transacción. Todo lo que se haga dentro de ejecutar(...)
 * se confirma junto o se deshace junto (ej: debitar al cliente y acreditar al proveedor).
 * La implementación JPA vive en infraestructura; el caso de uso no conoce EntityManager.
 */
public interface UnidadDeTrabajo {

    <T> T ejecutar(Supplier<T> trabajo);

    default void ejecutarAccion(Runnable accion) {
        ejecutar(() -> {
            accion.run();
            return null;
        });
    }
}
