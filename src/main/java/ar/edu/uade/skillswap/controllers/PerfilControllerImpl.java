package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.aplicacion.GestionPerfil;
import ar.edu.uade.skillswap.model.Models;

public class PerfilControllerImpl implements UiControllers.PerfilController {

    private final GestionPerfil gestion;

    public PerfilControllerImpl(GestionPerfil gestion) {
        this.gestion = gestion;
    }

    @Override
    public Models.Perfil verPerfil(String userId) {
        return ManejadorErrores.ejecutar("verPerfil", () -> {
            gestion.validarSesion(userId);
            return MapeadorDto.perfil(gestion.ver(userId));
        });
    }
}
