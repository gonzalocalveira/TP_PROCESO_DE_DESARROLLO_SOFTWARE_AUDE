package ar.edu.uade.skillswap.infraestructura;

import ar.edu.uade.skillswap.aplicacion.puertos.Comprobante;
import ar.edu.uade.skillswap.aplicacion.puertos.PagoRechazadoException;
import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.notificacion.Notificacion;
import ar.edu.uade.skillswap.infraestructura.identidad.IdentidadMockAdapter;
import ar.edu.uade.skillswap.infraestructura.identidad.ServicioIdentidadSDK;
import ar.edu.uade.skillswap.infraestructura.notificaciones.EmailAdapter;
import ar.edu.uade.skillswap.infraestructura.notificaciones.EmailProviderSDK;
import ar.edu.uade.skillswap.infraestructura.notificaciones.PushAdapter;
import ar.edu.uade.skillswap.infraestructura.notificaciones.PushProviderSDK;
import ar.edu.uade.skillswap.infraestructura.pagos.PasarelaSkillCoinsAdapter;
import ar.edu.uade.skillswap.infraestructura.pagos.SkillCoinsGatewaySDK;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static ar.edu.uade.skillswap.soporte.Fixtures.AHORA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** PATRÓN ADAPTER: cada adapter traduce nuestra interfaz a la API del SDK externo. */
class AdapterTest {

    @Test
    void pasarelaConvierteACentavosYSignoNegativoParaCreditos() {
        List<Long> cobros = new ArrayList<>();
        SkillCoinsGatewaySDK sdk = new SkillCoinsGatewaySDK(1_000_000) {
            @Override
            public String charge(long centavos) {
                cobros.add(centavos);
                return super.charge(centavos);
            }
        };
        PasarelaSkillCoinsAdapter adapter = new PasarelaSkillCoinsAdapter(sdk, Clock.systemDefaultZone());
        Comprobante debito = adapter.debitar("user-a", SkillCoins.de(150));
        adapter.acreditar("user-b", SkillCoins.de(20));
        assertEquals(List.of(15_000L, -2_000L), cobros);
        assertTrue(debito.codigo().startsWith("TX-"));
    }

    @Test
    void pasarelaTraduceErroresAExcepcionDeNegocio() {
        PasarelaSkillCoinsAdapter adapter = new PasarelaSkillCoinsAdapter(new SkillCoinsGatewaySDK(10_000), Clock.systemDefaultZone());
        PagoRechazadoException e = assertThrows(PagoRechazadoException.class, () -> adapter.debitar("user-a", SkillCoins.de(500)));
        assertTrue(e.getMessage().contains("límite"));
    }

    @Test
    void identidadInterpretaLosCodigosDelSdk() {
        ServicioIdentidadSDK sdk = new ServicioIdentidadSDK();
        sdk.registrar("user-a", "Ana");
        IdentidadMockAdapter adapter = new IdentidadMockAdapter(sdk);
        assertTrue(adapter.validar("user-a"));
        assertEquals("Ana", adapter.nombreDe("user-a"));
        assertFalse(adapter.validar("user-x"));
        sdk.suspender("user-a");
        assertFalse(adapter.validar("user-a"));
        assertThrows(PermisoDenegadoException.class, () -> adapter.nombreDe("user-a"));
    }

    @Test
    void emailYPushArmanElFormatoDeCadaProveedor() {
        EmailProviderSDK email = new EmailProviderSDK();
        PushProviderSDK push = new PushProviderSDK();
        Notificacion n = new Notificacion("user-ana", "PAGO", "Pago \"confirmado\"", "Detalle", AHORA);
        new EmailAdapter(email, id -> "ana@demo.com").enviar(n);
        new PushAdapter(push).enviar(n);
        assertTrue(email.enviados().get(0).contains("\"to\":\"ana@demo.com\""));
        assertTrue(email.enviados().get(0).contains("Pago \\\"confirmado\\\""));
        assertTrue(push.enviados().get(0).contains("\"device\":\"user-ana\""));
    }

    @Test
    void emailFallidoSeTraduceAExcepcion() {
        EmailProviderSDK roto = new EmailProviderSDK() {
            @Override
            public String send(String json) {
                return "550 mailbox unavailable";
            }
        };
        Notificacion n = new Notificacion("user-ana", "PAGO", "t", "d", AHORA);
        assertThrows(IllegalStateException.class, () -> new EmailAdapter(roto, id -> "x@y").enviar(n));
    }
}
