package ar.edu.uade.skillswap.infraestructura.identidad;

import ar.edu.uade.skillswap.aplicacion.puertos.ServicioIdentidad;
import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;

/**
 * PATRÓN ADAPTER — Adapter (por composición).
 * Implementa la interfaz que espera la aplicación (ServicioIdentidad = Target) y por dentro
 * traduce cada llamada a la API del SDK externo (Adaptee): arma el cuerpo "sub=...", elige el
 * código de operación e interpreta los códigos de respuesta.
 * Si mañana cambiamos de proveedor de identidad, se escribe otro adapter; casos de uso y
 * controllers no se tocan.
 */
public class IdentidadMockAdapter implements ServicioIdentidad {

    private final ServicioIdentidadSDK sdk;

    public IdentidadMockAdapter(ServicioIdentidadSDK sdk) {
        this.sdk = sdk;
    }

    @Override
    public boolean validar(String userId) {
        return sdk.consultar(ServicioIdentidadSDK.OP_VALIDAR, "sub=" + userId) == 200;
    }

    @Override
    public String nombreDe(String userId) {
        int codigo = sdk.consultar(ServicioIdentidadSDK.OP_PERFIL, "sub=" + userId);
        if (codigo != 200) {
            throw new PermisoDenegadoException("Identidad no disponible para " + userId + " (código " + codigo + ").");
        }
        return sdk.ultimaRespuesta().substring("nombre=".length());
    }
}
