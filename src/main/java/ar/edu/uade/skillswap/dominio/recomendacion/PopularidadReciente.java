package ar.edu.uade.skillswap.dominio.recomendacion;

import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * PATRÓN STRATEGY — Estrategia concreta.
 * Prioriza lo más contratado y lo publicado recientemente (útil para usuarios nuevos sin habilidades cargadas).
 */
public class PopularidadReciente implements RecomendacionStrategy {

    private final Clock reloj;
    private final int diasRecientes;

    public PopularidadReciente(Clock reloj, int diasRecientes) {
        this.reloj = reloj;
        this.diasRecientes = diasRecientes;
    }

    @Override
    public List<Publicacion> recomendar(Perfil perfil, List<Publicacion> candidatas) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        return candidatas.stream()
                .sorted(Comparator.comparingDouble((Publicacion p) -> puntaje(p, ahora)).reversed())
                .toList();
    }

    double puntaje(Publicacion p, LocalDateTime ahora) {
        double puntos = p.getCuposOcupados() * 2.0;
        LocalDateTime publicada = p.getFechaPublicacion() == null ? p.getFechaCreacion() : p.getFechaPublicacion();
        long dias = Math.max(0, Duration.between(publicada, ahora).toDays());
        if (dias <= diasRecientes) {
            puntos += (diasRecientes - dias) / (double) diasRecientes * 3;
        }
        return puntos;
    }

    @Override
    public String nombre() {
        return "Popularidad reciente";
    }
}
