package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.aplicacion.GestionContratacion;
import ar.edu.uade.skillswap.aplicacion.GestionPerfil;
import ar.edu.uade.skillswap.aplicacion.comandos.SolicitarCmd;
import ar.edu.uade.skillswap.aplicacion.vistas.ContratacionDetalle;
import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.model.Models;

import java.util.List;

/**
 * Controller de contrataciones. La UI tiene un único botón "Avanzar estado": acá se traduce al
 * paso que corresponde (aceptar, pagar, iniciar, completar). Igual el dominio (STATE + permisos)
 * vuelve a validar: si el usuario no puede hacer ese paso, recibe un mensaje claro.
 */
public class ContratacionControllerImpl implements UiControllers.ContratacionController {

    private final GestionContratacion gestion;
    private final GestionPerfil sesion;

    public ContratacionControllerImpl(GestionContratacion gestion, GestionPerfil sesion) {
        this.gestion = gestion;
        this.sesion = sesion;
    }

    @Override
    public List<Models.Contratacion> listarContrataciones(String userId) {
        return ManejadorErrores.ejecutar("listarContrataciones", () -> {
            sesion.validarSesion(userId);
            return gestion.listar(userId).stream().map(MapeadorDto::contratacion).toList();
        });
    }

    @Override
    public Models.Contratacion solicitar(String userId, String publicacionId, String objetivo) {
        return ManejadorErrores.ejecutar("solicitar", () -> {
            sesion.validarSesion(userId);
            return MapeadorDto.contratacion(gestion.solicitar(new SolicitarCmd(userId, publicacionId, null, objetivo, objetivo)));
        });
    }

    @Override
    public Models.Contratacion avanzar(String userId, String contratacionId) {
        return ManejadorErrores.ejecutar("avanzar", () -> {
            sesion.validarSesion(userId);
            String estado = gestion.ver(userId, contratacionId).contratacion().getEstado().nombre();
            ContratacionDetalle r = switch (estado) {
                case "PENDIENTE" -> gestion.aceptar(userId, contratacionId);
                case "ACEPTADA" -> gestion.pagar(userId, contratacionId);
                case "CONFIRMADA" -> gestion.iniciar(userId, contratacionId);
                case "EN_CURSO" -> gestion.completar(userId, contratacionId);
                default -> throw new TransicionInvalidaException(estado, "avanzar");
            };
            return MapeadorDto.contratacion(r);
        });
    }

    @Override
    public Models.Contratacion cancelar(String userId, String contratacionId) {
        return ManejadorErrores.ejecutar("cancelar", () -> {
            sesion.validarSesion(userId);
            Contratacion c = gestion.ver(userId, contratacionId).contratacion();
            boolean proveedorSinCobrar = c.esProveedor(userId) && !c.getEstado().estaPagada() && !c.getEstado().esFinal();
            ContratacionDetalle r = proveedorSinCobrar
                    ? gestion.rechazar(userId, contratacionId, "Rechazada por el proveedor")
                    : gestion.cancelar(userId, contratacionId, "Cancelada desde la app");
            return MapeadorDto.contratacion(r);
        });
    }
}
