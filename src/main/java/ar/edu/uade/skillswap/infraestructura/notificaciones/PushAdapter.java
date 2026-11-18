package ar.edu.uade.skillswap.infraestructura.notificaciones;

import ar.edu.uade.skillswap.aplicacion.puertos.Notificador;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

/** PATRÓN ADAPTER — Adapter: Notificador (Target) -> PushProviderSDK.push(json) (Adaptee). */
public class PushAdapter implements Notificador {

    private final PushProviderSDK sdk;

    public PushAdapter(PushProviderSDK sdk) {
        this.sdk = sdk;
    }

    @Override
    public void enviar(Notificacion n) {
        String json = "{\"device\":" + Json.texto(n.getDestinatarioId())
                + ",\"title\":" + Json.texto(n.getTitulo())
                + ",\"alert\":" + Json.texto(n.getMensaje()) + "}";
        String respuesta = sdk.push(json);
        if (!respuesta.contains("queued")) {
            throw new IllegalStateException("El proveedor push rechazó el envío: " + respuesta);
        }
    }

    @Override
    public String canal() {
        return "push";
    }
}
