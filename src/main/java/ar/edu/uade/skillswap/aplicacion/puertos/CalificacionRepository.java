package ar.edu.uade.skillswap.aplicacion.puertos;

import ar.edu.uade.skillswap.dominio.contratacion.Calificacion;

import java.util.List;

public interface CalificacionRepository {

    void guardar(Calificacion calificacion);

    boolean existe(String contratacionId, String autorId);

    List<Calificacion> porCalificado(String perfilId);
}
