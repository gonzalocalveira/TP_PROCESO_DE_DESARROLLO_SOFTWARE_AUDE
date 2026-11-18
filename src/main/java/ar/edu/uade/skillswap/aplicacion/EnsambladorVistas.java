package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.puertos.PerfilRepository;
import ar.edu.uade.skillswap.aplicacion.vistas.ContratacionDetalle;
import ar.edu.uade.skillswap.aplicacion.vistas.PublicacionDetalle;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.perfil.Perfil;
import ar.edu.uade.skillswap.dominio.precio.ComisionDecorator;
import ar.edu.uade.skillswap.dominio.precio.PoliticaPrecios;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Arma los modelos de lectura (publicación + autor + precio final). Se usa SIEMPRE dentro de
 * una transacción, y resuelve los autores en una sola consulta (evita N+1).
 */
public class EnsambladorVistas {

    private final PerfilRepository perfiles;
    private final PoliticaPrecios politicaPrecios;

    public EnsambladorVistas(PerfilRepository perfiles, PoliticaPrecios politicaPrecios) {
        this.perfiles = perfiles;
        this.politicaPrecios = politicaPrecios;
    }

    public List<PublicacionDetalle> publicaciones(List<Publicacion> lista) {
        Map<String, Perfil> autores = perfilesPorId(lista.stream().map(Publicacion::getAutorId).toList());
        return lista.stream().map(p -> publicacion(p, autores.get(p.getAutorId()))).toList();
    }

    public PublicacionDetalle publicacion(Publicacion p) {
        return publicacion(p, perfiles.porId(p.getAutorId()).orElse(null));
    }

    private PublicacionDetalle publicacion(Publicacion p, Perfil autor) {
        // PATRÓN DECORATOR: el mismo calculador que se usa para cobrar, así la UI muestra lo que se paga
        ComisionDecorator precio = politicaPrecios.precioParaCliente(p);
        return new PublicacionDetalle(p,
                autor == null ? "(sin autor)" : autor.getNombreVisible(),
                autor == null ? 0 : autor.getReputacion().valor(),
                precio.calcular(), precio.descripcion());
    }

    public List<ContratacionDetalle> contrataciones(List<Contratacion> lista, String userId) {
        Map<String, Perfil> otros = perfilesPorId(lista.stream().map(c -> c.contraparteDe(userId)).toList());
        return lista.stream().map(c -> {
            Perfil otro = otros.get(c.contraparteDe(userId));
            return new ContratacionDetalle(c, otro == null ? "(desconocido)" : otro.getNombreVisible(), c.esCliente(userId));
        }).toList();
    }

    public ContratacionDetalle contratacion(Contratacion c, String userId) {
        return contrataciones(List.of(c), userId).get(0);
    }

    private Map<String, Perfil> perfilesPorId(List<String> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return perfiles.porIds(ids.stream().distinct().toList()).stream()
                .collect(Collectors.toMap(Perfil::getId, Function.identity()));
    }
}
