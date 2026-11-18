package ar.edu.uade.skillswap.infraestructura.identidad;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SIMULA una librería EXTERNA de identidad (no la podemos modificar).
 * Su API es "de bajo nivel" e incompatible con lo que necesita SkillSwap:
 *  - se opera con códigos numéricos de operación y un cuerpo "clave=valor",
 *  - responde códigos HTTP-like (200, 400, 404, 410) y el dato hay que pedirlo aparte.
 * PATRÓN ADAPTER — Adaptee.
 */
public class ServicioIdentidadSDK {

    public static final int OP_VALIDAR = 1;
    public static final int OP_PERFIL = 2;

    private final Map<String, String> usuarios = new ConcurrentHashMap<>();
    private final Map<String, Boolean> suspendidos = new ConcurrentHashMap<>();
    private final ThreadLocal<String> ultimaRespuesta = new ThreadLocal<>();

    /** Operación administrativa del proveedor (en la demo se usa para registrar usuarios). */
    public void registrar(String sub, String nombre) {
        usuarios.put(sub, nombre);
    }

    public void suspender(String sub) {
        suspendidos.put(sub, true);
    }

    public int consultar(int codigo, String cuerpo) {
        if (cuerpo == null || !cuerpo.startsWith("sub=")) {
            ultimaRespuesta.set("error=cuerpo invalido");
            return 400;
        }
        String sub = cuerpo.substring(4);
        if (!usuarios.containsKey(sub)) {
            ultimaRespuesta.set("error=no encontrado");
            return 404;
        }
        if (suspendidos.containsKey(sub)) {
            ultimaRespuesta.set("error=suspendido");
            return 410;
        }
        switch (codigo) {
            case OP_VALIDAR:
                ultimaRespuesta.set("valido=true");
                return 200;
            case OP_PERFIL:
                ultimaRespuesta.set("nombre=" + usuarios.get(sub));
                return 200;
            default:
                ultimaRespuesta.set("error=operacion desconocida");
                return 400;
        }
    }

    public String ultimaRespuesta() {
        return ultimaRespuesta.get();
    }
}
