package ar.edu.uade.skillswap.aplicacion;

import java.util.List;

/** Categorías habilitadas en la plataforma (configurables en un solo lugar). */
public final class CatalogoCategorias {
    public static final String TODAS = "Todas";
    public static final List<String> CATEGORIAS =
            List.of("Tecnología", "Diseño", "Creatividad", "Negocios", "Productividad", "Idiomas");

    private CatalogoCategorias() {
    }

    public static boolean existe(String categoria) {
        return CATEGORIAS.contains(categoria);
    }
}
