package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.aplicacion.GestionPerfil;
import ar.edu.uade.skillswap.aplicacion.GestionPublicacion;
import ar.edu.uade.skillswap.aplicacion.comandos.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.comandos.CrearPublicacionCmd;
import ar.edu.uade.skillswap.aplicacion.comandos.FranjaCmd;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;
import ar.edu.uade.skillswap.model.Models;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Controller de "Mis publicaciones". Valida la sesión y que el usuario sea el autor (en el caso de uso). */
public class PublicacionControllerImpl implements UiControllers.PublicacionController {

    static final int CUPO_POR_DEFECTO = 5;
    static final int FRANJAS_POR_DEFECTO = 5;

    private final GestionPublicacion gestion;
    private final GestionPerfil sesion;
    private final Clock reloj;

    public PublicacionControllerImpl(GestionPublicacion gestion, GestionPerfil sesion, Clock reloj) {
        this.gestion = gestion;
        this.sesion = sesion;
        this.reloj = reloj;
    }

    @Override
    public List<Models.Publicacion> listarPropias(String userId) {
        return ManejadorErrores.ejecutar("listarPropias", () -> {
            sesion.validarSesion(userId);
            return gestion.listarPropias(userId).stream().map(MapeadorDto::publicacion).toList();
        });
    }

    @Override
    public Models.Publicacion crearBorrador(String userId, String titulo, String descripcion, String categoria,
                                           Models.Modalidad modalidad, int duracion, int precio) {
        return ManejadorErrores.ejecutar("crearBorrador", () -> {
            sesion.validarSesion(userId);
            // La UI del kit no permite elegir tipo, cupo ni agenda: se usan valores por defecto razonables.
            CrearPublicacionCmd cmd = new CrearPublicacionCmd(userId, TipoPublicacion.MENTORIA, titulo, descripcion,
                    categoria, Modalidad.valueOf(modalidad.name()), duracion, CUPO_POR_DEFECTO, precio,
                    List.of(categoria), agendaPorDefecto(duracion));
            return MapeadorDto.publicacion(gestion.crearBorrador(cmd));
        });
    }

    /** PATRÓN STATE: el controller traduce el estado pedido por la UI a la acción; el estado decide si es válida. */
    @Override
    public Models.Publicacion cambiarEstado(String userId, String publicacionId, Models.EstadoPublicacion estado) {
        return ManejadorErrores.ejecutar("cambiarEstado", () -> {
            sesion.validarSesion(userId);
            AccionPublicacion accion = switch (estado) {
                case PUBLICADA -> AccionPublicacion.PUBLICAR;
                case PAUSADA -> AccionPublicacion.PAUSAR;
                case CERRADA -> AccionPublicacion.CERRAR;
                case CANCELADA -> AccionPublicacion.CANCELAR;
                case BORRADOR -> throw new ReglaNegocioException("Una publicación no puede volver a BORRADOR.");
            };
            return MapeadorDto.publicacion(gestion.cambiarEstado(userId, publicacionId, accion, "Cambio desde la app"));
        });
    }

    private List<FranjaCmd> agendaPorDefecto(int duracion) {
        List<FranjaCmd> franjas = new ArrayList<>();
        LocalDate dia = LocalDate.now(reloj).plusDays(1);
        for (int i = 0; i < FRANJAS_POR_DEFECTO; i++) {
            LocalDateTime inicio = dia.plusDays(i).atTime(19, 0);
            franjas.add(new FranjaCmd(inicio, inicio.plusMinutes(Math.max(duracion, 15))));
        }
        return franjas;
    }
}
