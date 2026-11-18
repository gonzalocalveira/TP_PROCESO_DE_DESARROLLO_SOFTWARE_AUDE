package ar.edu.uade.skillswap.soporte;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.publicacion.FabricaPublicaciones;
import ar.edu.uade.skillswap.dominio.publicacion.FranjaDisponibilidad;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;

import java.time.LocalDateTime;

/** Objetos de dominio listos para usar en tests unitarios (sin base de datos). */
public final class Fixtures {

    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 13, 10, 0);
    public static final String AUTOR = "user-autor";
    public static final String CLIENTE = "user-cliente";

    private Fixtures() {
    }

    public static Publicacion borrador(int precio, int cupo) {
        Publicacion p = FabricaPublicaciones.crear(TipoPublicacion.MENTORIA, AUTOR, "Java avanzado", "Streams y concurrencia",
                "Tecnología", Modalidad.REMOTA, 60, cupo, SkillCoins.de(precio), AHORA);
        for (int i = 1; i <= 3; i++) {
            LocalDateTime inicio = AHORA.plusDays(i * 2L);
            p.agregarFranja(new FranjaDisponibilidad(inicio, inicio.plusHours(1)));
        }
        return p;
    }

    public static Publicacion publicada(int precio, int cupo) {
        Publicacion p = borrador(precio, cupo);
        p.publicar(AHORA);
        return p;
    }

    /** Contratación PENDIENTE con la primera franja (a 2 días de AHORA). */
    public static Contratacion contratacionPendiente(Publicacion p) {
        FranjaDisponibilidad f = p.reservarPrimeraDisponible(AHORA);
        return Contratacion.solicitar(p, CLIENTE, f, "Hola", "Aprender", SkillCoins.de(105), SkillCoins.de(5), AHORA);
    }
}
