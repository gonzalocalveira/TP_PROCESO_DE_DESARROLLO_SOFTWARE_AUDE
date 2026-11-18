package ar.edu.uade.skillswap.infraestructura.persistencia;

/** Aplica las migraciones de esquema (db/migration) sobre la base configurada. */
public interface MigradorEsquema {

    void migrar(String url, String usuario, String password);
}
