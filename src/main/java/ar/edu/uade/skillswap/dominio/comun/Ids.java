package ar.edu.uade.skillswap.dominio.comun;

import java.util.UUID;

/** Generación de identificadores en el dominio (no dependemos de la base para tener identidad). */
public final class Ids {
    private Ids() {
    }

    public static String nuevo(String prefijo) {
        return prefijo + "-" + UUID.randomUUID().toString().substring(0, 13);
    }
}
