package ar.edu.uade.skillswap.aplicacion.vistas;

import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;

/** Modelo de lectura: contratación vista desde un usuario (con el nombre de la contraparte). */
public record ContratacionDetalle(Contratacion contratacion, String contraparteNombre, boolean soyCliente) {
}
