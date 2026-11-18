package ar.edu.uade.skillswap.dominio;

import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.publicacion.Desafio;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.soporte.Fixtures;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** PATRÓN STATE en Publicacion. */
class EstadoPublicacionTest {

    @Test
    void cicloBorradorPublicadaPausadaPublicadaCerrada() {
        Publicacion p = Fixtures.borrador(100, 3);
        assertEquals("BORRADOR", p.getEstado().nombre());
        assertFalse(p.aceptaSolicitudes());
        p.publicar(Fixtures.AHORA);
        assertEquals("PUBLICADA", p.getEstado().nombre());
        assertTrue(p.aceptaSolicitudes());
        p.pausar();
        assertEquals("PAUSADA", p.getEstado().nombre());
        assertFalse(p.aceptaSolicitudes());
        p.publicar(Fixtures.AHORA);
        p.cerrar();
        assertEquals("CERRADA", p.getEstado().nombre());
    }

    @Test
    void transicionesInvalidasLanzanExcepcion() {
        Publicacion p = Fixtures.borrador(100, 3);
        assertThrows(TransicionInvalidaException.class, p::pausar);
        p.publicar(Fixtures.AHORA);
        assertThrows(TransicionInvalidaException.class, () -> p.publicar(Fixtures.AHORA));
        p.cancelar("ya no la ofrezco");
        assertThrows(TransicionInvalidaException.class, () -> p.publicar(Fixtures.AHORA));
        assertThrows(TransicionInvalidaException.class, p::cerrar);
    }

    @Test
    void noSePuedePublicarSinFranjasNiPrecio() {
        Publicacion sinFranjas = new ar.edu.uade.skillswap.dominio.publicacion.OfertaServicio(Fixtures.AUTOR, "CV",
                "Revisión de CV", "Negocios", Modalidad.REMOTA, 30, 1, SkillCoins.de(50), Fixtures.AHORA);
        assertThrows(ReglaNegocioException.class, () -> sinFranjas.publicar(Fixtures.AHORA));
        assertEquals("BORRADOR", sinFranjas.getEstado().nombre());
    }

    @Test
    void cadaSubclaseAgregaSusValidaciones() {
        Desafio individual = new Desafio(Fixtures.AUTOR, "Katas", "30 días", "Tecnología", Modalidad.REMOTA, 60, 1,
                SkillCoins.de(50), Fixtures.AHORA);
        individual.agregarFranja(new ar.edu.uade.skillswap.dominio.publicacion.FranjaDisponibilidad(
                Fixtures.AHORA.plusDays(1), Fixtures.AHORA.plusDays(1).plusHours(1)));
        assertThrows(ReglaNegocioException.class, () -> individual.publicar(Fixtures.AHORA));
        assertEquals("Desafío", individual.tipo());
    }

    @Test
    void reglaDeCupoYFranjaOcupada() {
        Publicacion p = Fixtures.publicada(100, 1);
        String franja = p.getFranjas().get(0).getId();
        p.reservar(franja, Fixtures.AHORA);
        assertThrows(ReglaNegocioException.class, () -> p.reservar(p.getFranjas().get(1).getId(), Fixtures.AHORA),
                "superar el cupo");
        p.liberar(franja);
        assertEquals(0, p.getCuposOcupados());
        p.reservar(franja, Fixtures.AHORA);
        Publicacion otra = Fixtures.publicada(100, 5);
        String f = otra.getFranjas().get(0).getId();
        otra.reservar(f, Fixtures.AHORA);
        assertThrows(ReglaNegocioException.class, () -> otra.reservar(f, Fixtures.AHORA), "franja ocupada");
    }

    @Test
    void unaPublicacionNoPublicadaNoAdmiteReservas() {
        Publicacion p = Fixtures.borrador(100, 3);
        assertThrows(ReglaNegocioException.class, () -> p.reservarPrimeraDisponible(Fixtures.AHORA));
    }
}
