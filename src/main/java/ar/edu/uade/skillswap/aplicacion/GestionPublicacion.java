package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.comandos.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.comandos.CrearPublicacionCmd;
import ar.edu.uade.skillswap.aplicacion.comandos.FranjaCmd;
import ar.edu.uade.skillswap.aplicacion.puertos.PerfilRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.PublicacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.aplicacion.vistas.PublicacionDetalle;
import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.evento.EventoDominio;
import ar.edu.uade.skillswap.dominio.evento.PublicadorEventos;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.publicacion.FabricaPublicaciones;
import ar.edu.uade.skillswap.dominio.publicacion.FranjaDisponibilidad;
import ar.edu.uade.skillswap.dominio.publicacion.Promocion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** Caso de uso: alta de borradores, cambios de estado (State), promociones y franjas. */
public class GestionPublicacion {

    private final PublicacionRepository publicaciones;
    private final PerfilRepository perfiles;
    private final UnidadDeTrabajo tx;
    private final PublicadorEventos eventos;
    private final EnsambladorVistas vistas;
    private final Clock reloj;

    public GestionPublicacion(PublicacionRepository publicaciones, PerfilRepository perfiles, UnidadDeTrabajo tx,
                              PublicadorEventos eventos, EnsambladorVistas vistas, Clock reloj) {
        this.publicaciones = publicaciones;
        this.perfiles = perfiles;
        this.tx = tx;
        this.eventos = eventos;
        this.vistas = vistas;
        this.reloj = reloj;
    }

    public PublicacionDetalle crearBorrador(CrearPublicacionCmd cmd) {
        if (!CatalogoCategorias.existe(cmd.categoria())) {
            throw new ReglaNegocioException("Categoría inexistente: " + cmd.categoria());
        }
        return tx.ejecutar(() -> {
            perfiles.obtener(cmd.autorId()).exigirActivo();
            LocalDateTime ahora = LocalDateTime.now(reloj);
            Publicacion p = FabricaPublicaciones.crear(cmd.tipo(), cmd.autorId(), cmd.titulo(), cmd.descripcion(),
                    cmd.categoria(), cmd.modalidad(), cmd.duracionMin(), cmd.cupo(), SkillCoins.de(cmd.precio()), ahora);
            cmd.etiquetas().forEach(p::agregarEtiqueta);
            for (FranjaCmd f : cmd.franjas()) {
                p.agregarFranja(new FranjaDisponibilidad(f.inicio(), f.fin()));
            }
            publicaciones.guardar(p);
            return vistas.publicacion(p);
        });
    }

    /**
     * PATRÓN STATE en acción: el caso de uso NO pregunta "si está en BORRADOR entonces...".
     * Solo pide la acción y el estado actual decide si es válida.
     */
    public PublicacionDetalle cambiarEstado(String actor, String publicacionId, AccionPublicacion accion, String motivo) {
        PublicacionDetalle resultado = tx.ejecutar(() -> {
            Publicacion p = publicacionPropia(actor, publicacionId);
            switch (accion) {
                case PUBLICAR -> p.publicar(LocalDateTime.now(reloj));
                case PAUSAR -> p.pausar();
                case CERRAR -> p.cerrar();
                case CANCELAR -> p.cancelar(motivo);
            }
            publicaciones.guardar(p);
            return vistas.publicacion(p);
        });
        Publicacion p = resultado.publicacion();
        eventos.publicar(new EventoDominio(TipoEvento.PUBLICACION_ACTUALIZADA, LocalDateTime.now(reloj), p.getId(),
                List.of(actor), "Publicación " + p.getEstado().nombre().toLowerCase(),
                "“" + p.getTitulo() + "” ahora está " + p.getEstado().nombre() + "."));
        return resultado;
    }

    public List<PublicacionDetalle> listarPropias(String autorId) {
        return tx.ejecutar(() -> vistas.publicaciones(publicaciones.porAutor(autorId).stream()
                .sorted(Comparator.comparing(Publicacion::getFechaCreacion).reversed()).toList()));
    }

    /** PATRÓN DECORATOR: cada promoción agregada será una capa más sobre el precio base. */
    public PublicacionDetalle agregarPromocion(String actor, String publicacionId, String descripcion,
                                               double porcentaje, int montoFijo) {
        return tx.ejecutar(() -> {
            Publicacion p = publicacionPropia(actor, publicacionId);
            p.agregarPromocion(new Promocion(descripcion, porcentaje, SkillCoins.de(montoFijo)));
            publicaciones.guardar(p);
            return vistas.publicacion(p);
        });
    }

    public PublicacionDetalle agregarFranja(String actor, String publicacionId, FranjaCmd franja) {
        return tx.ejecutar(() -> {
            Publicacion p = publicacionPropia(actor, publicacionId);
            p.agregarFranja(new FranjaDisponibilidad(franja.inicio(), franja.fin()));
            publicaciones.guardar(p);
            return vistas.publicacion(p);
        });
    }

    private Publicacion publicacionPropia(String actor, String publicacionId) {
        Publicacion p = publicaciones.obtener(publicacionId);
        if (!p.esDeAutor(actor)) {
            throw new PermisoDenegadoException("Solo el autor puede modificar esta publicación.");
        }
        return p;
    }
}
