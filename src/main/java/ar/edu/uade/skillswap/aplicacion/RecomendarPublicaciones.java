package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.puertos.PerfilRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.PublicacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.aplicacion.vistas.PublicacionDetalle;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.recomendacion.RecomendacionStrategy;

import java.util.List;
import java.util.Objects;

/**
 * Caso de uso: recomendaciones para un usuario.
 *
 * PATRÓN STRATEGY — Contexto.
 * Mantiene una referencia a RecomendacionStrategy y le delega el ordenamiento. La estrategia se
 * puede cambiar en tiempo de ejecución con setEstrategia() (ej: desde configuración) sin tocar
 * este código: el caso de uso se ocupa de filtrar candidatas y recortar; la estrategia, de ordenar.
 */
public class RecomendarPublicaciones {

    private final PublicacionRepository publicaciones;
    private final PerfilRepository perfiles;
    private final UnidadDeTrabajo tx;
    private final EnsambladorVistas vistas;
    private RecomendacionStrategy estrategia;

    public RecomendarPublicaciones(PublicacionRepository publicaciones, PerfilRepository perfiles, UnidadDeTrabajo tx,
                                   EnsambladorVistas vistas, RecomendacionStrategy estrategia) {
        this.publicaciones = publicaciones;
        this.perfiles = perfiles;
        this.tx = tx;
        this.vistas = vistas;
        this.estrategia = Objects.requireNonNull(estrategia);
    }

    public List<PublicacionDetalle> recomendar(String userId, int limite) {
        return tx.ejecutar(() -> {
            Perfil perfil = perfiles.obtener(userId);
            List<Publicacion> candidatas = publicaciones.publicadas().stream()
                    .filter(p -> !p.esDeAutor(userId))          // no se recomienda lo propio
                    .filter(Publicacion::tieneCupo)
                    .toList();
            List<Publicacion> ordenadas = estrategia.recomendar(perfil, candidatas); // <- delegación Strategy
            return vistas.publicaciones(ordenadas.stream().limit(limite).toList());
        });
    }

    public void setEstrategia(RecomendacionStrategy estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia);
    }

    public RecomendacionStrategy getEstrategia() {
        return estrategia;
    }
}
