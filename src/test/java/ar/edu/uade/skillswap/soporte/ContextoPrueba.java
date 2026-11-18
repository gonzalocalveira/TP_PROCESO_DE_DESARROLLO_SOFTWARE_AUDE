package ar.edu.uade.skillswap.soporte;

import ar.edu.uade.skillswap.aplicacion.comandos.CrearPerfilCmd;
import ar.edu.uade.skillswap.aplicacion.comandos.CrearPublicacionCmd;
import ar.edu.uade.skillswap.aplicacion.comandos.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.comandos.FranjaCmd;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;
import ar.edu.uade.skillswap.infraestructura.config.Configuracion;
import ar.edu.uade.skillswap.infraestructura.config.RelojAjustable;
import ar.edu.uade.skillswap.infraestructura.config.SkillSwapContexto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Levanta TODO el sistema real (casos de uso + Hibernate + migraciones) sobre una base H2
 * en memoria distinta para cada test, con validación del mapeo contra el esquema.
 */
public final class ContextoPrueba {

    private ContextoPrueba() {
    }

    public static SkillSwapContexto nuevo(boolean conDatosDemo) {
        Configuracion cfg = Configuracion.cargar()
                .con("db.modo", "h2")
                .con("db.h2.url", "jdbc:h2:mem:t" + UUID.randomUUID().toString().replace("-", "")
                        + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000")
                .con("db.validarEsquema", "true")
                .con("demo.cargarDatos", String.valueOf(conDatosDemo));
        return SkillSwapContexto.iniciar(cfg, RelojAjustable.delSistema());
    }

    public static void perfil(SkillSwapContexto ctx, String id, int saldo, String... habilidades) {
        ctx.identidadSdk().registrar(id, id);
        ctx.gestionPerfil().crear(new CrearPerfilCmd(id, "Nombre " + id, "Rol", "Bio", null, List.of(habilidades)));
        if (saldo > 0) {
            ctx.tx().ejecutarAccion(() -> ctx.wallets().obtener(id)
                    .acreditar(SkillCoins.de(saldo), "Saldo inicial", "TEST", LocalDateTime.now(ctx.reloj())));
        }
    }

    /** Crea y publica una mentoría con franjas diarias desde dentro de diasHastaPrimera días. */
    public static String publicacion(SkillSwapContexto ctx, String autor, String titulo, String categoria, int precio,
                                     int cupo, int diasHastaPrimera, String... etiquetas) {
        List<FranjaCmd> franjas = new ArrayList<>();
        LocalDate dia = LocalDate.now(ctx.reloj()).plusDays(diasHastaPrimera);
        for (int i = 0; i < 4; i++) {
            LocalDateTime inicio = dia.plusDays(i).atTime(18, 0);
            franjas.add(new FranjaCmd(inicio, inicio.plusHours(1)));
        }
        String id = ctx.gestionPublicacion().crearBorrador(new CrearPublicacionCmd(autor, TipoPublicacion.MENTORIA, titulo,
                "Descripción de " + titulo, categoria, Modalidad.REMOTA, 60, cupo, precio, List.of(etiquetas), franjas))
                .publicacion().getId();
        ctx.gestionPublicacion().cambiarEstado(autor, id, AccionPublicacion.PUBLICAR, null);
        return id;
    }

    public static int saldo(SkillSwapContexto ctx, String userId) {
        return ctx.gestionWallet().saldo(userId).aEntero();
    }

    public static void irA(SkillSwapContexto ctx, LocalDateTime momento) {
        ctx.reloj().desplazar(Duration.between(LocalDateTime.now(), momento));
    }
}
