package ar.edu.uade.skillswap.infraestructura.persistencia;

import org.flywaydb.core.Flyway;

/**
 * Migraciones versionadas con Flyway: aplica en orden los scripts V1__..., V2__... de
 * src/main/resources/db/migration y registra cuáles ya corrió (tabla flyway_schema_history).
 * Así la base de cualquier integrante del equipo queda en la misma versión.
 */
public class MigradorFlyway implements MigradorEsquema {

    @Override
    public void migrar(String url, String usuario, String password) {
        Flyway.configure()
                .dataSource(url, usuario, password)
                .locations("classpath:db/migration")
                .load()
                .migrate();
    }
}
