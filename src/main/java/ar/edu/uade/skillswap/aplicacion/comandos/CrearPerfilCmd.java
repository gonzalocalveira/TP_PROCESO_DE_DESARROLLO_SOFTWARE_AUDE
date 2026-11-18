package ar.edu.uade.skillswap.aplicacion.comandos;

import java.util.List;

public record CrearPerfilCmd(String userId, String nombre, String rol, String bio, String zonaHoraria,
                             List<String> habilidades) {
}
