package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.puertos.Notificador;
import ar.edu.uade.skillswap.dominio.evento.EventoDominio;
import ar.edu.uade.skillswap.dominio.evento.EventoObserver;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PATRÓN OBSERVER — Observador concreto.
 * Se suscribe al PublicadorEventos y, por cada evento, arma una Notificacion por destinatario
 * y la envía por TODOS los canales configurados.
 *
 * PATRÓN ADAPTER — Cliente.
 * Los canales son Notificador (Target): no sabe si detrás hay un proveedor de email, uno de push
 * o la bandeja interna. Agregar un canal = agregar un adapter en la configuración.
 */
public class NotificacionObserver implements EventoObserver {

    private static final Logger LOG = Logger.getLogger(NotificacionObserver.class.getName());

    private final List<Notificador> canales;

    public NotificacionObserver(List<Notificador> canales) {
        this.canales = List.copyOf(canales);
    }

    @Override
    public void notificar(EventoDominio e) {
        for (String destinatario : e.getDestinatarios()) {
            Notificacion n = new Notificacion(destinatario, e.getTipo().categoria(), e.getTitulo(), e.getDetalle(), e.getFecha());
            for (Notificador canal : canales) {
                try {
                    canal.enviar(n);
                } catch (RuntimeException ex) {
                    // un canal caído (ej: el proveedor de email) no impide los demás
                    LOG.log(Level.WARNING, "Falló el canal " + canal.canal() + " para " + destinatario, ex);
                }
            }
        }
    }
}
