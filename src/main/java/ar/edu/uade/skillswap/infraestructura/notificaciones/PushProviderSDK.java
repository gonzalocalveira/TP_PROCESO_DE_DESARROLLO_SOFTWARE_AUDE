package ar.edu.uade.skillswap.infraestructura.notificaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/** SIMULA el SDK de notificaciones push de un tercero. PATRÓN ADAPTER — Adaptee. */
public class PushProviderSDK {

    private static final Logger LOG = Logger.getLogger(PushProviderSDK.class.getName());
    private final List<String> enviados = Collections.synchronizedList(new ArrayList<>());

    public String push(String json) {
        if (json == null || !json.contains("\"device\"")) {
            return "{\"status\":\"rejected\"}";
        }
        enviados.add(json);
        LOG.fine(() -> "PUSH " + json);
        return "{\"status\":\"queued\"}";
    }

    public List<String> enviados() {
        return List.copyOf(enviados);
    }
}
