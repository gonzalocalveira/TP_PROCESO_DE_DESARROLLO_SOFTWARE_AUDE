package ar.edu.uade.skillswap.dominio.evento;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PATRÓN OBSERVER — Sujeto (publicador).
 * Mantiene la lista de observers y les avisa de cada evento. Los casos de uso solo llaman a
 * publicar(evento): no saben si después se manda un mail, un push, se guarda una notificación
 * interna o se audita. Agregar un interesado nuevo = suscribir otro observer (Open/Closed).
 *
 * Si un observer falla, se registra el error y se sigue con los demás: una notificación
 * caída no debe deshacer una contratación ya confirmada.
 */
public class PublicadorEventos {

    private static final Logger LOG = Logger.getLogger(PublicadorEventos.class.getName());

    private final List<EventoObserver> observers = new CopyOnWriteArrayList<>();

    public void suscribir(EventoObserver o) {
        observers.add(o);
    }

    public void desuscribir(EventoObserver o) {
        observers.remove(o);
    }

    public void publicar(EventoDominio e) {
        for (EventoObserver o : observers) {
            try {
                o.notificar(e);
            } catch (RuntimeException ex) {
                LOG.log(Level.WARNING, "El observer " + o.getClass().getSimpleName() + " falló con " + e, ex);
            }
        }
    }

    public void publicarTodos(List<EventoDominio> eventos) {
        eventos.forEach(this::publicar);
    }
}
