package ar.edu.uade.skillswap.infraestructura.config;

import ar.edu.uade.skillswap.aplicacion.comandos.CalificarCmd;
import ar.edu.uade.skillswap.aplicacion.comandos.CrearPerfilCmd;
import ar.edu.uade.skillswap.aplicacion.comandos.SolicitarCmd;
import ar.edu.uade.skillswap.aplicacion.vistas.ContratacionDetalle;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.perfil.Reputacion;
import ar.edu.uade.skillswap.dominio.publicacion.FabricaPublicaciones;
import ar.edu.uade.skillswap.dominio.publicacion.FranjaDisponibilidad;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.Promocion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;
import ar.edu.uade.skillswap.dominio.wallet.Wallet;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Datos de demostración. Se cargan SOLO si la base está vacía.
 * Las contrataciones de ejemplo se generan con los MISMOS casos de uso que usa la app
 * (moviendo el reloj hacia atrás), así quedan con historial, movimientos de wallet y
 * notificaciones coherentes.
 */
class DatosDemo {

    private static final Logger LOG = Logger.getLogger(DatosDemo.class.getName());

    static final String MATEO = "user-mateo";

    private record Usuario(String id, String nombre, String rol, String bio, List<String> habilidades,
                           double reputacion, int resenas, int proyectos, int saldoInicial) {
    }

    private static final List<Usuario> USUARIOS = List.of(
            new Usuario(MATEO, "Mateo Silva", "Software Engineer & Mentor",
                    "Construyo productos digitales y acompaño a personas que quieren crecer en tecnología.",
                    List.of("Java", "Clean Architecture", "Mentoría", "SQL", "Diseño OO"), 4.9, 37, 51, 2500),
            new Usuario("user-sofia", "Sofía Méndez", "Backend Developer & Tech Recruiter",
                    "Ayudo a desarrolladores a prepararse para entrevistas técnicas.", List.of("Java", "Entrevistas"), 4.9, 41, 60, 1000),
            new Usuario("user-tomas", "Tomás Vidal", "Product Designer",
                    "Diseño de producto y portfolios que cuentan historias.", List.of("UX", "Portfolio"), 4.8, 28, 35, 1000),
            new Usuario("user-camila", "Camila Torres", "Arquitecta de Software",
                    "Arquitectura limpia, DDD y buenas prácticas en Java.", List.of("Clean Architecture", "SOLID", "Java"), 5.0, 19, 24, 1000),
            new Usuario("user-nicolas", "Nicolás Paz", "Fotógrafo",
                    "Fotografía de producto y contenido para redes.", List.of("Fotografía"), 4.7, 22, 30, 1000),
            new Usuario("user-laura", "Laura Rossi", "Coach de liderazgo",
                    "Acompaño a líderes de equipos técnicos.", List.of("Liderazgo", "Equipos"), 4.9, 33, 40, 1000),
            new Usuario("user-martin", "Martín Aguirre", "Analista de datos",
                    "Excel y datos para tomar mejores decisiones.", List.of("Excel", "Datos"), 4.6, 15, 18, 1000),
            new Usuario("user-lucia", "Lucía Fernández", "Estudiante de Sistemas",
                    "Quiero aprender Java y buenas prácticas.", List.of("Java"), 0, 0, 0, 800));

    private final SkillSwapContexto ctx;
    private final LocalDateTime ahoraReal;

    DatosDemo(SkillSwapContexto ctx) {
        this.ctx = ctx;
        this.ahoraReal = LocalDateTime.now();
    }

    /** El proveedor de identidad es externo y en memoria: se registran los usuarios en cada arranque. */
    void registrarIdentidades() {
        USUARIOS.forEach(u -> ctx.identidadSdk().registrar(u.id(), u.nombre()));
    }

    void cargarSiEstaVacia() {
        if (ctx.tx().ejecutar(() -> ctx.perfiles().contar()) > 0) {
            return;
        }
        LOG.info("Base vacía: cargando datos de demo...");
        try {
            cargar();
        } finally {
            ctx.reloj().restablecer();
        }
    }

    private void cargar() {
        // -------------------------------------------------------------- hace 30 días: perfiles y publicaciones
        irA(ahoraReal.minusDays(30));
        for (Usuario u : USUARIOS) {
            ctx.gestionPerfil().crear(new CrearPerfilCmd(u.id(), u.nombre(), u.rol(), u.bio(), null, u.habilidades()));
        }
        ctx.tx().ejecutarAccion(() -> {
            for (Usuario u : USUARIOS) {
                Perfil p = ctx.perfiles().obtener(u.id());
                p.importarHistorial(new Reputacion(u.reputacion(), u.resenas()), u.proyectos());
                Wallet w = ctx.wallets().obtener(u.id());
                w.acreditar(SkillCoins.de(u.saldoInicial()), "Saldo inicial de bienvenida", "BIENVENIDA", ahora());
            }
        });

        LocalDate desde = ahoraReal.toLocalDate().plusDays(2);
        String p1 = publicar(TipoPublicacion.MENTORIA, "user-sofia", "Preparación para entrevistas técnicas",
                "Simulacro completo, feedback y plan personalizado para tu próxima entrevista.", "Tecnología",
                Modalidad.REMOTA, 60, 180, List.of("Java", "Entrevistas", "Backend"), true, desde, 18);
        String p2 = publicar(TipoPublicacion.SERVICIO, "user-tomas", "Revisión profesional de portfolio",
                "Mejorá la narrativa y presentación de tus proyectos para destacarte.", "Diseño",
                Modalidad.REMOTA, 45, 120, List.of("UX", "Portfolio", "Carrera"), true, desde, 18);
        publicar(TipoPublicacion.MENTORIA, "user-camila", "Arquitectura limpia aplicada",
                "Sesión práctica para ordenar dependencias y casos de uso en proyectos Java.", "Tecnología",
                Modalidad.HIBRIDA, 90, 260, List.of("Clean Architecture", "SOLID", "Java"), true, desde, 19);
        publicar(TipoPublicacion.SERVICIO, "user-nicolas", "Fotografía de producto con celular",
                "Iluminación, encuadre y edición simple para catálogos y redes.", "Creatividad",
                Modalidad.PRESENCIAL, 75, 150, List.of("Fotografía", "Contenido"), false, desde, 10);
        String p5 = publicar(TipoPublicacion.MENTORIA, "user-laura", "Mentoría de liderazgo para equipos",
                "Herramientas concretas para conversaciones, feedback y delegación.", "Negocios",
                Modalidad.REMOTA, 60, 220, List.of("Liderazgo", "Equipos"), false, desde, 17);
        String p6 = publicar(TipoPublicacion.SERVICIO, "user-martin", "Excel para decisiones de negocio",
                "Tablas dinámicas, escenarios y un dashboard listo para reutilizar.", "Productividad",
                Modalidad.REMOTA, 90, 140, List.of("Excel", "Datos"), false, desde, 20);
        String p7 = publicar(TipoPublicacion.DESAFIO, "user-sofia", "Desafío: 30 katas de Java",
                "Un mes de ejercicios guiados con revisión grupal semanal.", "Tecnología",
                Modalidad.REMOTA, 60, 90, List.of("Java", "Katas", "Práctica"), false, desde, 21);
        String m1 = publicar(TipoPublicacion.MENTORIA, MATEO, "Mentoría Java desde cero",
                "Fundamentos de Java y POO con ejercicios prácticos y seguimiento.", "Tecnología",
                Modalidad.REMOTA, 60, 100, List.of("Java", "POO"), false, desde, 19);
        crearBorrador(MATEO, "Code review de proyectos Spring",
                "Revisamos tu proyecto Spring y armamos un plan de mejoras.", desde);

        ctx.tx().ejecutarAccion(() -> {
            Publicacion excel = ctx.publicaciones().obtener(p6);
            excel.agregarPromocion(new Promocion("Lanzamiento", 20, SkillCoins.cero()));              // Decorator
            Publicacion katas = ctx.publicaciones().obtener(p7);
            katas.agregarPromocion(new Promocion("Cupón comunidad", 0, SkillCoins.de(20)));           // Decorator
            ctx.publicaciones().obtener(m1).configurarAceptacionAutomatica(false);                   // Mateo acepta a mano
            Publicacion lider = ctx.publicaciones().obtener(p5);
            LocalDateTime pasada = ahoraReal.minusDays(4).toLocalDate().atTime(18, 0);
            lider.agregarFranja(new FranjaDisponibilidad(pasada, pasada.plusMinutes(60)));
        });

        // -------------------------------------------------------------- hace 10 días: Mateo contrata a Laura
        irA(ahoraReal.minusDays(10));
        String franjaPasada = ctx.tx().ejecutar(() -> ctx.publicaciones().obtener(p5).getFranjas().stream()
                .filter(f -> f.getInicio().isBefore(ahoraReal)).findFirst().orElseThrow().getId());
        ContratacionDetalle c2 = ctx.gestionContratacion().solicitar(
                new SolicitarCmd(MATEO, p5, franjaPasada, "Lidero un equipo nuevo", "Mejorar el feedback 1 a 1"));
        String idC2 = c2.contratacion().getId();
        ctx.gestionContratacion().pagar(MATEO, idC2);

        // -------------------------------------------------------------- hace 4 días: se dicta y se califica
        LocalDateTime sesion = c2.contratacion().getFechaSesion();
        irA(sesion.plusMinutes(2));
        ctx.gestionContratacion().iniciar("user-laura", idC2);
        irA(sesion.plusMinutes(62));
        ctx.gestionContratacion().completar("user-laura", idC2);
        irA(sesion.plusHours(3));
        ctx.gestionCalificacion().calificar(new CalificarCmd(MATEO, idC2, 5, "Excelente sesión, muy práctica."));
        ctx.gestionCalificacion().calificar(new CalificarCmd("user-laura", idC2, 5, "Muy comprometido y puntual."));

        // -------------------------------------------------------------- ayer: Mateo reserva y paga un portfolio review
        irA(ahoraReal.minusDays(1));
        ContratacionDetalle c1 = ctx.gestionContratacion().solicitar(
                new SolicitarCmd(MATEO, p2, null, "Busco feedback de mi portfolio", "Preparar búsqueda laboral"));
        ctx.gestionContratacion().pagar(MATEO, c1.contratacion().getId());

        // -------------------------------------------------------------- hace un rato: Lucía le pide una mentoría a Mateo
        irA(ahoraReal.minusHours(3));
        ctx.gestionContratacion().solicitar(
                new SolicitarCmd("user-lucia", m1, null, "Estoy empezando con Java", "Entender POO"));
        irA(ahoraReal.minusMinutes(40));
        ctx.gestionContratacion().solicitar(
                new SolicitarCmd("user-lucia", p1, null, "Tengo una entrevista pronto", "Practicar"));

        // las notificaciones viejas de Mateo ya las leyó
        ctx.reloj().restablecer();
        ctx.tx().ejecutarAccion(() -> ctx.notificaciones().porUsuario(MATEO).stream()
                .filter(n -> n.getFecha().isBefore(ahoraReal.minusDays(2)))
                .forEach(n -> n.marcarLeida(MATEO)));
        LOG.info("Datos de demo cargados.");
    }

    private String publicar(TipoPublicacion tipo, String autor, String titulo, String descripcion, String categoria,
                            Modalidad modalidad, int duracion, int precio, List<String> etiquetas, boolean destacada,
                            LocalDate desde, int hora) {
        return ctx.tx().ejecutar(() -> {
            Publicacion p = FabricaPublicaciones.crear(tipo, autor, titulo, descripcion, categoria, modalidad,
                    duracion, tipo == TipoPublicacion.DESAFIO ? 20 : 10, SkillCoins.de(precio), ahora());
            etiquetas.forEach(p::agregarEtiqueta);
            agenda(p, desde, hora, duracion);
            p.destacar(destacada);
            p.configurarAceptacionAutomatica(true);
            p.publicar(ahora());                                       // State: BORRADOR -> PUBLICADA
            ctx.publicaciones().guardar(p);
            return p.getId();
        });
    }

    private void crearBorrador(String autor, String titulo, String descripcion, LocalDate desde) {
        ctx.tx().ejecutarAccion(() -> {
            Publicacion p = FabricaPublicaciones.crear(TipoPublicacion.SERVICIO, autor, titulo, descripcion, "Tecnología",
                    Modalidad.REMOTA, 45, 5, SkillCoins.de(130), ahora());
            List.of("Java", "Spring", "Code review").forEach(p::agregarEtiqueta);
            agenda(p, desde, 20, 45);
            ctx.publicaciones().guardar(p);                            // queda en BORRADOR
        });
    }

    private static void agenda(Publicacion p, LocalDate desde, int hora, int duracion) {
        for (int i = 0; i < 12; i++) {
            LocalDateTime inicio = desde.plusDays(i).atTime(LocalTime.of(hora, 0));
            p.agregarFranja(new FranjaDisponibilidad(inicio, inicio.plusMinutes(duracion)));
        }
    }

    private void irA(LocalDateTime momento) {
        ctx.reloj().desplazar(Duration.between(LocalDateTime.now(), momento));
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(ctx.reloj());
    }
}
