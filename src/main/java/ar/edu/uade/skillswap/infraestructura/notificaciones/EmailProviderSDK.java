package ar.edu.uade.skillswap.infraestructura.notificaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * SIMULA el SDK de un proveedor de email externo. PATRÓN ADAPTER — Adaptee.
 * Recibe un JSON con campos en inglés y devuelve un código estilo SMTP como texto.
 */
public class EmailProviderSDK {

    private static final Logger LOG = Logger.getLogger(EmailProviderSDK.class.getName());
    private final List<String> enviados = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong secuencia = new AtomicLong();

    public String send(String json) {
        if (json == null || !json.contains("\"to\"")) {
            return "550 missing recipient";
        }
        enviados.add(json);
        LOG.fine(() -> "EMAIL " + json);
        return "250 OK id=" + secuencia.incrementAndGet();
    }

    public List<String> enviados() {
        return List.copyOf(enviados);
    }
}
