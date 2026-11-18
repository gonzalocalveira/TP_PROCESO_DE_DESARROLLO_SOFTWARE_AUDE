package ar.edu.uade.skillswap.infraestructura.persistencia;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Alternativa mínima a Flyway para la base EMBEBIDA (H2) de demo y tests: ejecuta los mismos
 * scripts de db/migration, en orden, si la base está vacía. En MySQL siempre se usa Flyway.
 */
public class MigradorScriptsSql implements MigradorEsquema {

    static final List<String> SCRIPTS = List.of("V1__esquema_inicial.sql", "V2__indices_busqueda.sql");

    @Override
    public void migrar(String url, String usuario, String password) {
        try (Connection cn = DriverManager.getConnection(url, usuario, password)) {
            if (existeTabla(cn, "perfiles")) {
                return;
            }
            for (String script : SCRIPTS) {
                ejecutar(cn, leer("db/migration/" + script));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo migrar la base embebida: " + e.getMessage(), e);
        }
    }

    private static boolean existeTabla(Connection cn, String tabla) throws SQLException {
        try (ResultSet rs = cn.getMetaData().getTables(null, null, tabla, null)) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = cn.getMetaData().getTables(null, null, tabla.toUpperCase(), null)) {
            return rs.next();
        }
    }

    private static void ejecutar(Connection cn, String sql) throws SQLException {
        StringBuilder limpio = new StringBuilder();
        for (String linea : sql.split("\n")) {
            if (!linea.strip().startsWith("--")) {
                limpio.append(linea).append('\n');
            }
        }
        try (Statement st = cn.createStatement()) {
            for (String sentencia : limpio.toString().split(";")) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia);
                }
            }
        }
    }

    private static String leer(String recurso) {
        try (InputStream in = MigradorScriptsSql.class.getClassLoader().getResourceAsStream(recurso)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + recurso);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
