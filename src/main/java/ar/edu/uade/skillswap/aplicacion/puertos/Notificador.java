package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

/**
 * PATRÓN ADAPTER — Target (interfaz que espera nuestro sistema).
 * Un canal de notificación. Los adapters (Email, Push, Interna) traducen esta llamada a la API
 * propia de cada proveedor externo, que es incompatible con la nuestra.
 */
public interface Notificador {

    void enviar(Notificacion notificacion);

    String canal();
}
