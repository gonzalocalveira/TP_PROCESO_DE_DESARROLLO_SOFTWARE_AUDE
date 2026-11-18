package ar.edu.uade.skillswap.dominio;

import ar.edu.uade.skillswap.dominio.cancelacion.ReglasCancelacion;
import ar.edu.uade.skillswap.dominio.cancelacion.ReintegroParcial;
import ar.edu.uade.skillswap.dominio.cancelacion.ReintegroTotal;
import ar.edu.uade.skillswap.dominio.cancelacion.SinReintegro;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.perfil.Habilidad;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.publicacion.FabricaPublicaciones;
import ar.edu.uade.skillswap.dominio.publicacion.FranjaDisponibilidad;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;
import ar.edu.uade.skillswap.dominio.recomendacion.AfinidadPorHabilidades;
import ar.edu.uade.skillswap.dominio.recomendacion.PopularidadReciente;
import ar.edu.uade.skillswap.soporte.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;
import java.util.List;

import static ar.edu.uade.skillswap.soporte.Fixtures.AHORA;
import static ar.edu.uade.skillswap.soporte.Fixtures.AUTOR;
import static ar.edu.uade.skillswap.soporte.Fixtures.CLIENTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** PATRÓN STRATEGY: política de cancelación y recomendaciones. */
class StrategyTest {

    private final ReglasCancelacion reglas = new ReglasCancelacion(48, 24, 50);

    @Test
    void laAnticipacionEligeLaEstrategiaDeReintegro() {
        Contratacion c = Fixtures.contratacionPendiente(Fixtures.publicada(100, 3)); // sesión a 48 h de AHORA
        assertInstanceOf(ReintegroTotal.class, reglas.politicaPara(c, CLIENTE, AHORA));
        assertInstanceOf(ReintegroParcial.class, reglas.politicaPara(c, CLIENTE, AHORA.plusHours(10)));
        assertInstanceOf(SinReintegro.class, reglas.politicaPara(c, CLIENTE, AHORA.plusHours(30)));
        assertInstanceOf(ReintegroTotal.class, reglas.politicaPara(c, AUTOR, AHORA.plusHours(47)), "cancela el proveedor");
    }

    @Test
    void cadaEstrategiaCalculaSuReintegro() {
        Contratacion c = Fixtures.contratacionPendiente(Fixtures.publicada(100, 3)); // total 105
        assertEquals(SkillCoins.de(105), new ReintegroTotal().calcularReintegro(c));
        assertEquals(SkillCoins.de(53), new ReintegroParcial(50).calcularReintegro(c));
        assertEquals(SkillCoins.cero(), new SinReintegro().calcularReintegro(c));
    }

    @Test
    void afinidadPriorizaLasHabilidadesDelPerfil() {
        Perfil perfil = new Perfil("user-p", "Pepa", "", "", null);
        perfil.agregarHabilidad(new Habilidad("Java", "Tecnología"));
        Publicacion excel = pub("Excel avanzado", "Productividad", "Excel");
        Publicacion java = pub("Spring con Java", "Tecnología", "Java");
        List<Publicacion> orden = new AfinidadPorHabilidades().recomendar(perfil, List.of(excel, java));
        assertEquals(java, orden.get(0));
    }

    @Test
    void popularidadPriorizaLoMasContratado() {
        Perfil perfil = new Perfil("user-p", "Pepa", "", "", null);
        Publicacion poco = pub("A", "Diseño", "UX");
        Publicacion mucho = pub("B", "Diseño", "UX");
        mucho.reservarPrimeraDisponible(AHORA);
        mucho.reservarPrimeraDisponible(AHORA);
        Clock reloj = Clock.fixed(AHORA.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        List<Publicacion> orden = new PopularidadReciente(reloj, 30).recomendar(perfil, List.of(poco, mucho));
        assertEquals(mucho, orden.get(0));
    }

    private static Publicacion pub(String titulo, String categoria, String etiqueta) {
        Publicacion p = FabricaPublicaciones.crear(TipoPublicacion.SERVICIO, AUTOR, titulo, "desc", categoria,
                Modalidad.REMOTA, 60, 5, SkillCoins.de(100), AHORA);
        p.agregarEtiqueta(etiqueta);
        for (int i = 1; i <= 3; i++) {
            p.agregarFranja(new FranjaDisponibilidad(AHORA.plusDays(i), AHORA.plusDays(i).plusHours(1)));
        }
        p.publicar(AHORA);
        return p;
    }
}
