package ar.edu.uade.skillswap.aplicacion.puertos;

/**
 * PATRÓN ADAPTER — Target.
 * Lo que la aplicación necesita del proveedor de identidad. La implementación
 * (IdentidadMockAdapter) adapta un SDK externo con otra interfaz.
 */
public interface ServicioIdentidad {

    /** true si el userId corresponde a una sesión/usuario válido en el proveedor de identidad. */
    boolean validar(String userId);

    String nombreDe(String userId);
}
