package ar.edu.uade.skillswap.infraestructura.config;

import ar.edu.uade.skillswap.aplicacion.AuditoriaObserver;
import ar.edu.uade.skillswap.aplicacion.BusquedaPublicaciones;
import ar.edu.uade.skillswap.aplicacion.EnsambladorVistas;
import ar.edu.uade.skillswap.aplicacion.GestionCalificacion;
import ar.edu.uade.skillswap.aplicacion.GestionContratacion;
import ar.edu.uade.skillswap.aplicacion.GestionNotificaciones;
import ar.edu.uade.skillswap.aplicacion.GestionPerfil;
import ar.edu.uade.skillswap.aplicacion.GestionPublicacion;
import ar.edu.uade.skillswap.aplicacion.GestionWallet;
import ar.edu.uade.skillswap.aplicacion.NotificacionObserver;
import ar.edu.uade.skillswap.aplicacion.RecomendarPublicaciones;
import ar.edu.uade.skillswap.aplicacion.puertos.CalificacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.ContratacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.NotificacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.Notificador;
import ar.edu.uade.skillswap.aplicacion.puertos.PerfilRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.ProcesadorPagos;
import ar.edu.uade.skillswap.aplicacion.puertos.PublicacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.ServicioIdentidad;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.aplicacion.puertos.WalletRepository;
import ar.edu.uade.skillswap.controllers.SkillSwapControllers;
import ar.edu.uade.skillswap.controllers.UiControllers;
import ar.edu.uade.skillswap.dominio.cancelacion.ReglasCancelacion;
import ar.edu.uade.skillswap.dominio.evento.PublicadorEventos;
import ar.edu.uade.skillswap.dominio.precio.PoliticaPrecios;
import ar.edu.uade.skillswap.dominio.recomendacion.AfinidadPorHabilidades;
import ar.edu.uade.skillswap.dominio.recomendacion.PopularidadReciente;
import ar.edu.uade.skillswap.dominio.recomendacion.RecomendacionStrategy;
import ar.edu.uade.skillswap.dominio.wallet.Wallet;
import ar.edu.uade.skillswap.infraestructura.identidad.IdentidadMockAdapter;
import ar.edu.uade.skillswap.infraestructura.identidad.ServicioIdentidadSDK;
import ar.edu.uade.skillswap.infraestructura.notificaciones.EmailAdapter;
import ar.edu.uade.skillswap.infraestructura.notificaciones.EmailProviderSDK;
import ar.edu.uade.skillswap.infraestructura.notificaciones.NotificacionInternaAdapter;
import ar.edu.uade.skillswap.infraestructura.notificaciones.PushAdapter;
import ar.edu.uade.skillswap.infraestructura.notificaciones.PushProviderSDK;
import ar.edu.uade.skillswap.infraestructura.pagos.PasarelaSkillCoinsAdapter;
import ar.edu.uade.skillswap.infraestructura.pagos.SkillCoinsGatewaySDK;
import ar.edu.uade.skillswap.infraestructura.persistencia.JpaUnidadDeTrabajo;
import ar.edu.uade.skillswap.infraestructura.persistencia.MigradorFlyway;
import ar.edu.uade.skillswap.infraestructura.persistencia.MigradorScriptsSql;
import ar.edu.uade.skillswap.infraestructura.persistencia.jpa.JpaCalificacionRepository;
import ar.edu.uade.skillswap.infraestructura.persistencia.jpa.JpaContratacionRepository;
import ar.edu.uade.skillswap.infraestructura.persistencia.jpa.JpaNotificacionRepository;
import ar.edu.uade.skillswap.infraestructura.persistencia.jpa.JpaPerfilRepository;
import ar.edu.uade.skillswap.infraestructura.persistencia.jpa.JpaPublicacionRepository;
import ar.edu.uade.skillswap.infraestructura.persistencia.jpa.JpaWalletRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Raíz de composición ("composition root"): el ÚNICO lugar que conoce las clases concretas.
 * Acá se elige qué adapter, qué estrategia y qué repositorio usar y se inyectan por constructor.
 * El resto del sistema depende solo de interfaces (Dependency Inversion).
 */
public final class SkillSwapContexto implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(SkillSwapContexto.class.getName());
    private static final Logger HIBERNATE = Logger.getLogger("org.hibernate");

    private final EntityManagerFactory emf;
    private final String descripcionBase;
    private final RelojAjustable reloj;

    // infraestructura "externa" simulada (Adaptees)
    private final ServicioIdentidadSDK identidadSdk = new ServicioIdentidadSDK();
    private final EmailProviderSDK emailSdk = new EmailProviderSDK();
    private final PushProviderSDK pushSdk = new PushProviderSDK();

    // puertos implementados
    private final UnidadDeTrabajo tx;
    private final PerfilRepository perfiles = new JpaPerfilRepository();
    private final PublicacionRepository publicaciones = new JpaPublicacionRepository();
    private final ContratacionRepository contrataciones = new JpaContratacionRepository();
    private final CalificacionRepository calificaciones = new JpaCalificacionRepository();
    private final WalletRepository wallets = new JpaWalletRepository();
    private final NotificacionRepository notificaciones = new JpaNotificacionRepository();

    // patrones
    private final PublicadorEventos publicador = new PublicadorEventos();
    private final AuditoriaObserver auditoria = new AuditoriaObserver();

    // casos de uso
    private final GestionPerfil gestionPerfil;
    private final GestionPublicacion gestionPublicacion;
    private final BusquedaPublicaciones busqueda;
    private final RecomendarPublicaciones recomendaciones;
    private final GestionContratacion gestionContratacion;
    private final GestionCalificacion gestionCalificacion;
    private final GestionNotificaciones gestionNotificaciones;
    private final GestionWallet gestionWallet;

    private final SkillSwapControllers controllers;

    private SkillSwapContexto(Configuracion cfg, RelojAjustable reloj) {
        HIBERNATE.setLevel(Level.WARNING);
        this.reloj = reloj;

        // ---------------------------------------------------------------- base de datos + migraciones
        BaseDatos bd = resolverBase(cfg);
        this.descripcionBase = bd.descripcion();
        Map<String, String> propiedades = new HashMap<>(Map.of(
                "jakarta.persistence.jdbc.url", bd.url(),
                "jakarta.persistence.jdbc.user", bd.usuario(),
                "jakarta.persistence.jdbc.password", bd.password()));
        if (cfg.booleano("db.validarEsquema", false)) {
            propiedades.put("hibernate.hbm2ddl.auto", "validate");   // el mapeo debe coincidir con las migraciones
        }
        this.emf = Persistence.createEntityManagerFactory("skillswap", propiedades);
        this.tx = new JpaUnidadDeTrabajo(emf);
        tx.ejecutarAccion(() -> {
            if (wallets.porPerfil(Wallet.PLATAFORMA).isEmpty()) {
                wallets.guardar(new Wallet(Wallet.PLATAFORMA));      // cuenta que recibe las comisiones
            }
        });

        // ---------------------------------------------------------------- PATRÓN ADAPTER: servicios externos
        ServicioIdentidad identidad = new IdentidadMockAdapter(identidadSdk);
        ProcesadorPagos pagos = new PasarelaSkillCoinsAdapter(
                new SkillCoinsGatewaySDK(cfg.largo("pasarela.limiteCentavos", 10_000_000L)), reloj);
        List<Notificador> canales = List.of(
                new NotificacionInternaAdapter(notificaciones, tx),
                new EmailAdapter(emailSdk, userId -> userId.replace("user-", "") + "@skillswap.demo"),
                new PushAdapter(pushSdk));

        // ---------------------------------------------------------------- PATRÓN OBSERVER: suscripciones
        publicador.suscribir(new NotificacionObserver(canales));
        publicador.suscribir(auditoria);

        // ---------------------------------------------------------------- PATRÓN DECORATOR / STRATEGY: configuración
        PoliticaPrecios politicaPrecios = new PoliticaPrecios(cfg.decimal("precio.comisionPorcentaje", 5));
        ReglasCancelacion reglasCancelacion = new ReglasCancelacion(
                cfg.largo("cancelacion.horasReintegroTotal", 48),
                cfg.largo("cancelacion.horasReintegroParcial", 24),
                cfg.decimal("cancelacion.porcentajeParcial", 50));
        RecomendacionStrategy estrategia = "popularidad".equalsIgnoreCase(cfg.texto("recomendacion.estrategia", "afinidad"))
                ? new PopularidadReciente(reloj, 30)
                : new AfinidadPorHabilidades();

        // ---------------------------------------------------------------- casos de uso
        EnsambladorVistas vistas = new EnsambladorVistas(perfiles, politicaPrecios);
        gestionPerfil = new GestionPerfil(perfiles, wallets, publicaciones, identidad, tx, reloj);
        gestionPublicacion = new GestionPublicacion(publicaciones, perfiles, tx, publicador, vistas, reloj);
        busqueda = new BusquedaPublicaciones(publicaciones, tx, vistas);
        recomendaciones = new RecomendarPublicaciones(publicaciones, perfiles, tx, vistas, estrategia);
        gestionContratacion = new GestionContratacion(contrataciones, publicaciones, wallets, perfiles, pagos,
                politicaPrecios, reglasCancelacion, tx, publicador, vistas, reloj);
        gestionCalificacion = new GestionCalificacion(calificaciones, contrataciones, perfiles, tx, publicador, reloj);
        gestionNotificaciones = new GestionNotificaciones(notificaciones, tx);
        gestionWallet = new GestionWallet(wallets, pagos, tx, publicador, reloj, cfg.entero("wallet.limiteCargaDemo", 5000));

        // ---------------------------------------------------------------- presentación
        controllers = new SkillSwapControllers(gestionPerfil, gestionPublicacion, busqueda, recomendaciones,
                gestionContratacion, gestionCalificacion, gestionNotificaciones, gestionWallet, reloj,
                cfg.entero("recomendacion.cantidad", 3));

        // ---------------------------------------------------------------- datos de demo
        DatosDemo demo = new DatosDemo(this);
        demo.registrarIdentidades();
        if (cfg.booleano("demo.cargarDatos", true)) {
            demo.cargarSiEstaVacia();
        }
    }

    public static SkillSwapContexto iniciar(Configuracion cfg) {
        return new SkillSwapContexto(cfg, RelojAjustable.delSistema());
    }

    public static SkillSwapContexto iniciar(Configuracion cfg, RelojAjustable reloj) {
        return new SkillSwapContexto(cfg, reloj);
    }

    // ==================================================================== elección de base

    private record BaseDatos(String url, String usuario, String password, String descripcion) {
    }

    private static BaseDatos resolverBase(Configuracion cfg) {
        String modo = cfg.texto("db.modo", "mysql");
        if ("mysql".equalsIgnoreCase(modo)) {
            String url = cfg.texto("db.mysql.url", "");
            String usuario = cfg.texto("db.mysql.usuario", "root");
            String password = cfg.texto("db.mysql.password", "");
            if (puedeConectar(url, usuario, password)) {
                new MigradorFlyway().migrar(url, usuario, password);
                return new BaseDatos(url, usuario, password, "MySQL");
            }
            if (!cfg.booleano("db.fallbackH2", true)) {
                throw new IllegalStateException("No se pudo conectar a MySQL (" + url + "). Revisá skillswap.properties.");
            }
            LOG.warning("MySQL no está disponible: se usa la base embebida H2 (los datos no se conservan al cerrar).");
        }
        String url = cfg.texto("db.h2.url", "jdbc:h2:mem:skillswap;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        String usuario = cfg.texto("db.h2.usuario", "sa");
        String password = cfg.texto("db.h2.password", "");
        new MigradorScriptsSql().migrar(url, usuario, password);
        return new BaseDatos(url, usuario, password, "base demo H2");
    }

    private static boolean puedeConectar(String url, String usuario, String password) {
        DriverManager.setLoginTimeout(3);
        try (Connection conexion = DriverManager.getConnection(url, usuario, password)) {
            return conexion.isValid(3);
        } catch (SQLException e) {
            LOG.log(Level.INFO, "No se pudo conectar a " + url + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }

    // ==================================================================== accesos

    public UiControllers controllers() {
        return controllers;
    }

    public String descripcionBase() {
        return descripcionBase;
    }

    public RelojAjustable reloj() {
        return reloj;
    }

    public UnidadDeTrabajo tx() {
        return tx;
    }

    public PerfilRepository perfiles() {
        return perfiles;
    }

    public PublicacionRepository publicaciones() {
        return publicaciones;
    }

    public ContratacionRepository contrataciones() {
        return contrataciones;
    }

    public WalletRepository wallets() {
        return wallets;
    }

    public NotificacionRepository notificaciones() {
        return notificaciones;
    }

    public PublicadorEventos publicador() {
        return publicador;
    }

    public AuditoriaObserver auditoria() {
        return auditoria;
    }

    public ServicioIdentidadSDK identidadSdk() {
        return identidadSdk;
    }

    public EmailProviderSDK emailSdk() {
        return emailSdk;
    }

    public PushProviderSDK pushSdk() {
        return pushSdk;
    }

    public GestionPerfil gestionPerfil() {
        return gestionPerfil;
    }

    public GestionPublicacion gestionPublicacion() {
        return gestionPublicacion;
    }

    public BusquedaPublicaciones busqueda() {
        return busqueda;
    }

    public RecomendarPublicaciones recomendaciones() {
        return recomendaciones;
    }

    public GestionContratacion gestionContratacion() {
        return gestionContratacion;
    }

    public GestionCalificacion gestionCalificacion() {
        return gestionCalificacion;
    }

    public GestionNotificaciones gestionNotificaciones() {
        return gestionNotificaciones;
    }

    public GestionWallet gestionWallet() {
        return gestionWallet;
    }
}
