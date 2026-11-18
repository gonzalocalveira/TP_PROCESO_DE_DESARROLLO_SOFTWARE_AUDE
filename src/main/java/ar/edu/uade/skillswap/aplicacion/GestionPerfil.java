package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.comandos.CrearPerfilCmd;
import ar.edu.uade.skillswap.aplicacion.puertos.PerfilRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.PublicacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.ServicioIdentidad;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.aplicacion.puertos.WalletRepository;
import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.perfil.Habilidad;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.wallet.Wallet;

import java.time.Clock;
import java.time.LocalDateTime;

/** Caso de uso: alta, consulta y baja lógica de perfiles; validación de sesión para los controllers. */
public class GestionPerfil {

    private final PerfilRepository perfiles;
    private final WalletRepository wallets;
    private final PublicacionRepository publicaciones;
    private final ServicioIdentidad identidad;   // PATRÓN ADAPTER: se usa a través del Target
    private final UnidadDeTrabajo tx;
    private final Clock reloj;

    public GestionPerfil(PerfilRepository perfiles, WalletRepository wallets, PublicacionRepository publicaciones,
                         ServicioIdentidad identidad, UnidadDeTrabajo tx, Clock reloj) {
        this.perfiles = perfiles;
        this.wallets = wallets;
        this.publicaciones = publicaciones;
        this.identidad = identidad;
        this.tx = tx;
        this.reloj = reloj;
    }

    public Perfil crear(CrearPerfilCmd cmd) {
        if (!identidad.validar(cmd.userId())) {
            throw new PermisoDenegadoException("El proveedor de identidad no reconoce al usuario " + cmd.userId() + ".");
        }
        return tx.ejecutar(() -> {
            if (perfiles.porId(cmd.userId()).isPresent()) {
                throw new ReglaNegocioException("Ya existe un perfil para " + cmd.userId() + ".");
            }
            Perfil p = new Perfil(cmd.userId(), cmd.nombre(), cmd.rol(), cmd.bio(), cmd.zonaHoraria());
            if (cmd.habilidades() != null) {
                cmd.habilidades().forEach(h -> p.agregarHabilidad(new Habilidad(h, null)));
            }
            perfiles.guardar(p);
            wallets.guardar(new Wallet(p.getId()));
            return p;
        });
    }

    public Perfil ver(String userId) {
        return tx.ejecutar(() -> perfiles.obtener(userId));
    }

    /** Baja lógica: el perfil queda inactivo y sus publicaciones visibles se pausan. */
    public void darDeBaja(String actor, String userId) {
        if (!actor.equals(userId)) {
            throw new PermisoDenegadoException("Solo el propio usuario puede darse de baja.");
        }
        tx.ejecutarAccion(() -> {
            Perfil p = perfiles.obtener(userId);
            p.darDeBaja(LocalDateTime.now(reloj));
            for (Publicacion pub : publicaciones.porAutor(userId)) {
                if (pub.aceptaSolicitudes()) {
                    pub.pausar();
                    publicaciones.guardar(pub);
                }
            }
            perfiles.guardar(p);
        });
    }

    /** Lo usan los controllers: el userId debe ser válido en el proveedor de identidad y estar activo. */
    public void validarSesion(String userId) {
        if (userId == null || userId.isBlank() || !identidad.validar(userId)) {
            throw new PermisoDenegadoException("Sesión inválida: el usuario no está autenticado.");
        }
        tx.ejecutar(() -> perfiles.obtener(userId)).exigirActivo();
    }
}
