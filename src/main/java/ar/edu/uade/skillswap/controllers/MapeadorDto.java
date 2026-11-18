package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.aplicacion.vistas.ContratacionDetalle;
import ar.edu.uade.skillswap.aplicacion.vistas.PublicacionDetalle;
import ar.edu.uade.skillswap.dominio.perfil.Habilidad;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.wallet.MovimientoCredito;
import ar.edu.uade.skillswap.model.Models;

/**
 * Traduce objetos de dominio a los DTO (records) que espera la UI del kit.
 * La UI nunca recibe entidades: así el dominio puede cambiar sin romper la vista.
 */
final class MapeadorDto {

    private MapeadorDto() {
    }

    static Models.Publicacion publicacion(PublicacionDetalle d) {
        Publicacion p = d.publicacion();
        return new Models.Publicacion(p.getId(), p.getTitulo(), p.getDescripcion(), p.getCategoria(),
                Models.Modalidad.valueOf(p.getModalidad().name()), p.getDuracionMin(), d.precioFinal().aEntero(),
                d.reputacionAutor(), d.autorNombre(), p.getEtiquetas(),
                Models.EstadoPublicacion.valueOf(p.getEstado().nombre()), p.isDestacada());
    }

    static Models.Contratacion contratacion(ContratacionDetalle d) {
        var c = d.contratacion();
        return new Models.Contratacion(c.getId(), c.getTitulo(), d.contraparteNombre(), c.getFechaSesion(),
                c.getTotal().aEntero(), Models.EstadoContratacion.valueOf(c.getEstado().nombre()));
    }

    static Models.Movimiento movimiento(MovimientoCredito m) {
        return new Models.Movimiento(m.getConcepto(), m.getFecha(), m.importeConSigno().aEntero(),
                m.getSaldoResultante().aEntero());
    }

    static Models.Notificacion notificacion(ar.edu.uade.skillswap.dominio.notificacion.Notificacion n) {
        return new Models.Notificacion(n.getId(), n.getTitulo(), n.getMensaje(), n.getFecha(), n.estaLeida(), n.getTipo());
    }

    static Models.Perfil perfil(ar.edu.uade.skillswap.dominio.perfil.Perfil p) {
        return new Models.Perfil(p.getId(), p.getNombreVisible(), p.getRol(), p.getBio(),
                p.getHabilidades().stream().map(Habilidad::getNombre).toList(),
                p.getReputacion().valor(), p.getReputacion().getCantidad(), p.getProyectosCompletados());
    }
}
