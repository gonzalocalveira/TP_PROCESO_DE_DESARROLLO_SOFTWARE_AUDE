package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.dominio.evento.EventoDominio;
import ar.edu.uade.skillswap.dominio.evento.EventoObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * PATRÓN OBSERVER — Segundo observador concreto: deja traza de cada evento en el log.
 * Muestra la ventaja del patrón: el mismo evento le llega a varios interesados sin que el
 * caso de uso que lo emitió sepa que existen.
 */
public class AuditoriaObserver implements EventoObserver {

    private static final Logger LOG = Logger.getLogger("skillswap.auditoria");
    private final List<String> ultimos = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void notificar(EventoDominio e) {
        String linea = e.getFecha() + " " + e;
        LOG.info(linea);
        ultimos.add(linea);
        if (ultimos.size() > 200) {
            ultimos.remove(0);
        }
    }

    public List<String> ultimos() {
        return List.copyOf(ultimos);
    }
}
