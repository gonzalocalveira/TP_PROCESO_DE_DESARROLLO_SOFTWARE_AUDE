package ar.edu.uade.skillswap.aplicacion.puertos;

import java.util.List;
import java.util.function.Function;

/** Resultado paginado. */
public record Pagina<T>(List<T> contenido, int pagina, int tamanio, long total) {

    public Pagina {
        contenido = List.copyOf(contenido);
    }

    public int totalPaginas() {
        return (int) Math.ceil(total / (double) tamanio);
    }

    public boolean hayMas() {
        return pagina + 1 < totalPaginas();
    }

    public <R> Pagina<R> map(Function<T, R> f) {
        return new Pagina<>(contenido.stream().map(f).toList(), pagina, tamanio, total);
    }
}
