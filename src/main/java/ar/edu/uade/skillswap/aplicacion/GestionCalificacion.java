package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.comandos.CalificarCmd;
import ar.edu.uade.skillswap.aplicacion.puertos.CalificacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.ContratacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.PerfilRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.contratacion.Calificacion;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.evento.EventoDominio;
import ar.edu.uade.skillswap.dominio.evento.PublicadorEventos;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Caso de uso: calificar a la contraparte de una contratación completada (una sola vez). */
public class GestionCalificacion {

    private final CalificacionRepository calificaciones;
    private final ContratacionRepository contrataciones;
    private final PerfilRepository perfiles;
    private final UnidadDeTrabajo tx;
    private final PublicadorEventos eventos;
    private final Clock reloj;

    public GestionCalificacion(CalificacionRepository calificaciones, ContratacionRepository contrataciones,
                               PerfilRepository perfiles, UnidadDeTrabajo tx, PublicadorEventos eventos, Clock reloj) {
        this.calificaciones = calificaciones;
        this.contrataciones = contrataciones;
        this.perfiles = perfiles;
        this.tx = tx;
        this.eventos = eventos;
        this.reloj = reloj;
    }

    public Calificacion calificar(CalificarCmd cmd) {
        Calificacion cal = tx.ejecutar(() -> {
            Contratacion c = contrataciones.obtener(cmd.contratacionId());
            if (calificaciones.existe(c.getId(), cmd.autorId())) {
                throw new ReglaNegocioException("Ya calificaste esta contratación.");
            }
            Calificacion nueva = Calificacion.crear(c, cmd.autorId(), cmd.puntaje(), cmd.comentario(),
                    LocalDateTime.now(reloj));
            Perfil calificado = perfiles.obtener(nueva.getCalificadoId());
            calificado.registrarCalificacion(nueva);
            calificaciones.guardar(nueva);
            perfiles.guardar(calificado);
            return nueva;
        });
        eventos.publicar(new EventoDominio(TipoEvento.CALIFICACION_RECIBIDA, LocalDateTime.now(reloj),
                cal.getContratacionId(), List.of(cal.getCalificadoId()), "Nueva calificación recibida",
                "Te calificaron con " + cal.getPuntaje() + " estrellas."));
        return cal;
    }
}
