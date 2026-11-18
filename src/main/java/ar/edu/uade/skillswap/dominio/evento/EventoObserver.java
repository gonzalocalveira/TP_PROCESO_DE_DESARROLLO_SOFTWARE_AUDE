package ar.edu.uade.skillswap.dominio.evento;

/**
 * PATRÓN OBSERVER — Observador.
 * Cualquier interesado en los eventos del dominio implementa esta interfaz y se suscribe al
 * PublicadorEventos. El que emite el evento no conoce a los observadores concretos.
 */
public interface EventoObserver {

    void notificar(EventoDominio evento);
}
