package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.aplicacion.BusquedaPublicaciones;
import ar.edu.uade.skillswap.aplicacion.GestionPerfil;
import ar.edu.uade.skillswap.aplicacion.RecomendarPublicaciones;
import ar.edu.uade.skillswap.aplicacion.puertos.FiltroBusqueda;
import ar.edu.uade.skillswap.aplicacion.puertos.OrdenBusqueda;
import ar.edu.uade.skillswap.dominio.publicacion.Modalidad;
import ar.edu.uade.skillswap.model.Models;

import java.util.List;

/** Controller de búsqueda y recomendaciones (implementa el contrato del kit). */
public class BusquedaControllerImpl implements UiControllers.BusquedaController {

    static final int TAMANIO_PAGINA_UI = 50;

    private final BusquedaPublicaciones busqueda;
    private final RecomendarPublicaciones recomendaciones;
    private final GestionPerfil sesion;
    private final int cantidadRecomendaciones;

    public BusquedaControllerImpl(BusquedaPublicaciones busqueda, RecomendarPublicaciones recomendaciones,
                                  GestionPerfil sesion, int cantidadRecomendaciones) {
        this.busqueda = busqueda;
        this.recomendaciones = recomendaciones;
        this.sesion = sesion;
        this.cantidadRecomendaciones = cantidadRecomendaciones;
    }

    @Override
    public List<Models.Publicacion> buscar(Models.FiltrosBusqueda f) {
        return ManejadorErrores.ejecutar("buscar", () -> {
            FiltroBusqueda filtro = new FiltroBusqueda(f.texto(), f.categoria(),
                    f.modalidad() == null ? null : Modalidad.valueOf(f.modalidad().name()),
                    f.precioMaximo(), orden(f.orden()), 0, TAMANIO_PAGINA_UI);
            return busqueda.buscar(filtro).contenido().stream().map(MapeadorDto::publicacion).toList();
        });
    }

    @Override
    public List<Models.Publicacion> recomendar(String userId) {
        return ManejadorErrores.ejecutar("recomendar", () -> {
            sesion.validarSesion(userId);
            return recomendaciones.recomendar(userId, cantidadRecomendaciones).stream()
                    .map(MapeadorDto::publicacion).toList();
        });
    }

    @Override
    public List<String> categorias() {
        return busqueda.categorias();
    }

    static OrdenBusqueda orden(String textoUi) {
        if (textoUi == null) {
            return OrdenBusqueda.RELEVANCIA;
        }
        return switch (textoUi) {
            case "Mejor reputación" -> OrdenBusqueda.MEJOR_REPUTACION;
            case "Menor precio" -> OrdenBusqueda.MENOR_PRECIO;
            default -> OrdenBusqueda.RELEVANCIA;
        };
    }
}
