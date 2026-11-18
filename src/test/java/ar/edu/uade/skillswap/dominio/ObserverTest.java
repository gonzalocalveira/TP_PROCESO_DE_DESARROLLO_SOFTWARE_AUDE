package ar.edu.uade.skillswap.dominio;

import ar.edu.uade.skillswap.aplicacion.NotificacionObserver;
import ar.edu.uade.skillswap.aplicacion.puertos.Notificador;
import ar.edu.uade.skillswap.dominio.evento.EventoDominio;
import ar.edu.uade.skillswap.dominio.evento.EventoObserver;
import ar.edu.uade.skillswap.dominio.evento.PublicadorEventos;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static ar.edu.uade.skillswap.soporte.Fixtures.AHORA;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** PATRÓN OBSERVER: un evento llega a todos los suscriptos, aunque alguno falle. */
class ObserverTest {

    private final EventoDominio evento = new EventoDominio(TipoEvento.PAGO_CONFIRMADO, AHORA, "con-1",
            List.of("user-a", "user-b"), "Pago confirmado", "Se pagaron 100");

    @Test
    void todosLosObserversRecibenElEvento() {
        PublicadorEventos publicador = new PublicadorEventos();
        List<String> recibidos = new ArrayList<>();
        EventoObserver roto = e -> {
            throw new IllegalStateException("caído");
        };
        publicador.suscribir(e -> recibidos.add("1:" + e.getTipo()));
        publicador.suscribir(roto);
        publicador.suscribir(e -> recibidos.add("2:" + e.getTipo()));
        publicador.publicar(evento);
        assertEquals(List.of("1:PAGO_CONFIRMADO", "2:PAGO_CONFIRMADO"), recibidos);
    }

    @Test
    void notificacionObserverEnviaPorTodosLosCanalesAunqueUnoFalle() {
        List<String> enviados = new ArrayList<>();
        Notificador ok = canal("interna", n -> enviados.add(n.getDestinatarioId()));
        Notificador roto = canal("email", n -> {
            throw new IllegalStateException("SMTP caído");
        });
        new NotificacionObserver(List.of(roto, ok)).notificar(evento);
        assertEquals(List.of("user-a", "user-b"), enviados);
    }

    private static Notificador canal(String nombre, java.util.function.Consumer<Notificacion> accion) {
        return new Notificador() {
            @Override
            public void enviar(Notificacion n) {
                accion.accept(n);
            }

            @Override
            public String canal() {
                return nombre;
            }
        };
    }
}
