package ar.edu.uade.skillswap.aplicacion.comandos;

/** franjaId nulo = tomar la próxima franja libre. */
public record SolicitarCmd(String clienteId, String publicacionId, String franjaId, String mensaje, String objetivo) {
}
