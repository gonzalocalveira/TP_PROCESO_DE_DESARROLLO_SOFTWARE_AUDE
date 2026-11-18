package ar.edu.uade.skillswap.dominio;

import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.contratacion.Calificacion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.contratacion.HistorialEstado;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.soporte.Fixtures;
import org.junit.jupiter.api.Test;

import java.util.List;

import static ar.edu.uade.skillswap.soporte.Fixtures.AHORA;
import static ar.edu.uade.skillswap.soporte.Fixtures.AUTOR;
import static ar.edu.uade.skillswap.soporte.Fixtures.CLIENTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** PATRÓN STATE en Contratacion + permisos + historial inmutable + calificación. */
class EstadoContratacionTest {

    @Test
    void caminoFelizConHistorial() {
        Contratacion c = Fixtures.contratacionPendiente(Fixtures.publicada(100, 3));
        c.aceptar(AUTOR, AHORA);
        c.pagar(CLIENTE, AHORA);
        c.iniciar(AUTOR, AHORA);
        c.completar(CLIENTE, AHORA);
        assertEquals("COMPLETADA", c.getEstado().nombre());
        List<HistorialEstado> h = c.getHistorial();
        assertEquals(5, h.size());
        assertEquals("ACEPTADA", h.get(1).getEstadoNuevo());
        assertEquals(AUTOR, h.get(1).getActor());
        assertEquals("Pago confirmado", h.get(2).getMotivo());
        assertEquals(AHORA, h.get(2).getFecha());
    }

    @Test
    void elHistorialNoSePuedeModificarDesdeAfuera() {
        Contratacion c = Fixtures.contratacionPendiente(Fixtures.publicada(100, 3));
        assertThrows(UnsupportedOperationException.class, () -> c.getHistorial().clear());
    }

    @Test
    void doblePagoEsInvalido() {
        Contratacion c = Fixtures.contratacionPendiente(Fixtures.publicada(100, 3));
        c.aceptar(AUTOR, AHORA);
        c.pagar(CLIENTE, AHORA);
        assertThrows(TransicionInvalidaException.class, () -> c.pagar(CLIENTE, AHORA));
    }

    @Test
    void noSePuedePagarAntesDeQueAcepteElProveedor() {
        Contratacion c = Fixtures.contratacionPendiente(Fixtures.publicada(100, 3));
        assertThrows(TransicionInvalidaException.class, () -> c.pagar(CLIENTE, AHORA));
    }

    @Test
    void cadaRolSoloPuedeHacerSusPasos() {
        Contratacion c = Fixtures.contratacionPendiente(Fixtures.publicada(100, 3));
        assertThrows(PermisoDenegadoException.class, () -> c.aceptar(CLIENTE, AHORA));
        c.aceptar(AUTOR, AHORA);
        assertThrows(PermisoDenegadoException.class, () -> c.pagar(AUTOR, AHORA));
        assertThrows(PermisoDenegadoException.class, () -> c.cancelar("user-intruso", "x", AHORA));
    }

    @Test
    void unaSesionEnCursoNoSePuedeCancelar() {
        Contratacion c = Fixtures.contratacionPendiente(Fixtures.publicada(100, 3));
        c.aceptar(AUTOR, AHORA);
        c.pagar(CLIENTE, AHORA);
        c.iniciar(AUTOR, AHORA);
        assertThrows(TransicionInvalidaException.class, () -> c.cancelar(CLIENTE, "me arrepentí", AHORA));
    }

    @Test
    void regla1ElAutorNoPuedeSolicitarSuPropiaPublicacion() {
        Publicacion p = Fixtures.publicada(100, 3);
        assertThrows(ReglaNegocioException.class, () -> Contratacion.solicitar(p, AUTOR, p.getFranjas().get(0), "", "",
                SkillCoins.de(100), SkillCoins.cero(), AHORA));
    }

    @Test
    void soloSeCalificaUnaContratacionCompletadaYALaContraparte() {
        Contratacion c = Fixtures.contratacionPendiente(Fixtures.publicada(100, 3));
        assertThrows(ReglaNegocioException.class, () -> Calificacion.crear(c, CLIENTE, 5, "", AHORA));
        c.aceptar(AUTOR, AHORA);
        c.pagar(CLIENTE, AHORA);
        c.iniciar(AUTOR, AHORA);
        c.completar(AUTOR, AHORA);
        Calificacion cal = Calificacion.crear(c, CLIENTE, 4, "Muy buena", AHORA);
        assertEquals(AUTOR, cal.getCalificadoId());
        assertTrue(cal.esVerificada());
        assertThrows(PermisoDenegadoException.class, () -> Calificacion.crear(c, "user-x", 5, "", AHORA));
        assertThrows(ReglaNegocioException.class, () -> Calificacion.crear(c, AUTOR, 6, "", AHORA));
    }
}
