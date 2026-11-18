package ar.edu.uade.skillswap.dominio.recomendacion;

import ar.edu.uade.skillswap.dominio.perfil.Habilidad;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

import java.util.Comparator;
import java.util.List;

/**
 * PATRÓN STRATEGY — Estrategia concreta.
 * Puntúa cada publicación según cuántas habilidades del perfil aparecen en sus etiquetas,
 * título o categoría. Las destacadas desempatan.
 */
public class AfinidadPorHabilidades implements RecomendacionStrategy {

    @Override
    public List<Publicacion> recomendar(Perfil perfil, List<Publicacion> candidatas) {
        return candidatas.stream()
                .sorted(Comparator.comparingInt((Publicacion p) -> puntaje(perfil, p)).reversed()
                        .thenComparing(Publicacion::isDestacada, Comparator.reverseOrder()))
                .toList();
    }

    int puntaje(Perfil perfil, Publicacion p) {
        int puntos = 0;
        for (Habilidad h : perfil.getHabilidades()) {
            for (String etiqueta : p.getEtiquetas()) {
                if (h.coincideCon(etiqueta)) {
                    puntos += 3;
                }
            }
            if (h.coincideCon(p.getTitulo())) {
                puntos += 2;
            }
            if (h.coincideCon(p.getCategoria()) || p.getCategoria().equalsIgnoreCase(h.getCategoria())) {
                puntos += 1;
            }
        }
        return puntos;
    }

    @Override
    public String nombre() {
        return "Afinidad por habilidades";
    }
}
