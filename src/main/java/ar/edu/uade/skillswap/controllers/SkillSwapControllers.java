package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.aplicacion.BusquedaPublicaciones;
import ar.edu.uade.skillswap.aplicacion.GestionCalificacion;
import ar.edu.uade.skillswap.aplicacion.GestionContratacion;
import ar.edu.uade.skillswap.aplicacion.GestionNotificaciones;
import ar.edu.uade.skillswap.aplicacion.GestionPerfil;
import ar.edu.uade.skillswap.aplicacion.GestionPublicacion;
import ar.edu.uade.skillswap.aplicacion.GestionWallet;
import ar.edu.uade.skillswap.aplicacion.RecomendarPublicaciones;

import java.time.Clock;

/**
 * Implementación REAL del contrato UiControllers que usa la vista Swing del profesor.
 * Reemplaza a MockSkillSwapControllers: la vista no cambia, solo cambia el objeto que recibe
 * (la UI depende de la interfaz, no de la implementación).
 */
public class SkillSwapControllers implements UiControllers {

    private final BusquedaController busqueda;
    private final PublicacionController publicaciones;
    private final ContratacionController contrataciones;
    private final WalletController wallet;
    private final NotificacionController notificaciones;
    private final PerfilController perfiles;
    private final CalificacionController calificaciones;

    public SkillSwapControllers(GestionPerfil gestionPerfil, GestionPublicacion gestionPublicacion,
                                BusquedaPublicaciones busqueda, RecomendarPublicaciones recomendaciones,
                                GestionContratacion gestionContratacion, GestionCalificacion gestionCalificacion,
                                GestionNotificaciones gestionNotificaciones, GestionWallet gestionWallet,
                                Clock reloj, int cantidadRecomendaciones) {
        this.busqueda = new BusquedaControllerImpl(busqueda, recomendaciones, gestionPerfil, cantidadRecomendaciones);
        this.publicaciones = new PublicacionControllerImpl(gestionPublicacion, gestionPerfil, reloj);
        this.contrataciones = new ContratacionControllerImpl(gestionContratacion, gestionPerfil);
        this.wallet = new WalletControllerImpl(gestionWallet, gestionPerfil);
        this.notificaciones = new NotificacionControllerImpl(gestionNotificaciones, gestionPerfil);
        this.perfiles = new PerfilControllerImpl(gestionPerfil);
        this.calificaciones = new CalificacionController(gestionCalificacion, gestionPerfil);
    }

    @Override
    public BusquedaController busqueda() {
        return busqueda;
    }

    @Override
    public PublicacionController publicaciones() {
        return publicaciones;
    }

    @Override
    public ContratacionController contrataciones() {
        return contrataciones;
    }

    @Override
    public WalletController wallet() {
        return wallet;
    }

    @Override
    public NotificacionController notificaciones() {
        return notificaciones;
    }

    @Override
    public PerfilController perfiles() {
        return perfiles;
    }

    public CalificacionController calificaciones() {
        return calificaciones;
    }
}
