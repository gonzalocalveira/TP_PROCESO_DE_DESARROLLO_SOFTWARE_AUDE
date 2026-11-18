package ar.edu.uade.skillswap.infraestructura.notificaciones;

import ar.edu.uade.skillswap.aplicacion.puertos.Notificador;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;

import java.util.function.Function;

/**
 * PATRÓN ADAPTER — Adapter.
 * Notificador (Target) -> EmailProviderSDK.send(json) (Adaptee).
 * Traduce nuestra Notificacion al JSON del proveedor y su respuesta "250 OK" / "550 ..." a
 * éxito o excepción.
 */
public class EmailAdapter implements Notificador {

    private final EmailProviderSDK sdk;
    private final Function<String, String> emailDe;

    public EmailAdapter(EmailProviderSDK sdk, Function<String, String> emailDe) {
        this.sdk = sdk;
        this.emailDe = emailDe;
    }

    @Override
    public void enviar(Notificacion n) {
        String json = "{\"to\":" + Json.texto(emailDe.apply(n.getDestinatarioId()))
                + ",\"subject\":" + Json.texto("[SkillSwap] " + n.getTitulo())
                + ",\"body\":" + Json.texto(n.getMensaje()) + "}";
        String respuesta = sdk.send(json);
        if (!respuesta.startsWith("250")) {
            throw new IllegalStateException("El proveedor de email rechazó el envío: " + respuesta);
        }
    }

    @Override
    public String canal() {
        return "email";
    }
}
