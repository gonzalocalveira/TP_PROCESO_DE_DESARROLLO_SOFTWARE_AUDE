package ar.edu.uade.skillswap.aplicacion.comandos;

import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.dominio.publicacion.TipoPublicacion;

import java.util.List;

public record CrearPublicacionCmd(String autorId, TipoPublicacion tipo, String titulo, String descripcion,
                                  String categoria, Modalidad modalidad, int duracionMin, int cupo, int precio,
                                  List<String> etiquetas, List<FranjaCmd> franjas) {
    public CrearPublicacionCmd {
        etiquetas = etiquetas == null ? List.of() : List.copyOf(etiquetas);
        franjas = franjas == null ? List.of() : List.copyOf(franjas);
    }
}
