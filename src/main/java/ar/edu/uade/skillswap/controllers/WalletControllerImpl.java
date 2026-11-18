package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.aplicacion.GestionPerfil;
import ar.edu.uade.skillswap.aplicacion.GestionWallet;
import ar.edu.uade.skillswap.model.Models;

import java.util.List;

public class WalletControllerImpl implements UiControllers.WalletController {

    private final GestionWallet gestion;
    private final GestionPerfil sesion;

    public WalletControllerImpl(GestionWallet gestion, GestionPerfil sesion) {
        this.gestion = gestion;
        this.sesion = sesion;
    }

    @Override
    public int consultarSaldo(String userId) {
        return ManejadorErrores.ejecutar("consultarSaldo", () -> {
            sesion.validarSesion(userId);
            return gestion.saldo(userId).aEntero();
        });
    }

    @Override
    public List<Models.Movimiento> listarMovimientos(String userId) {
        return ManejadorErrores.ejecutar("listarMovimientos", () -> {
            sesion.validarSesion(userId);
            return gestion.movimientos(userId).stream().map(MapeadorDto::movimiento).toList();
        });
    }

    @Override
    public int acreditarDemo(String userId, int importe) {
        return ManejadorErrores.ejecutar("acreditarDemo", () -> {
            sesion.validarSesion(userId);
            return gestion.acreditarDemo(userId, importe).aEntero();
        });
    }
}
