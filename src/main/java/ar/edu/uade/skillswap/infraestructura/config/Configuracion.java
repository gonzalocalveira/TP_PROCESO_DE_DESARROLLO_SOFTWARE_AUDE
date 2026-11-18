package ar.edu.uade.skillswap.infraestructura.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Lee skillswap.properties (classpath) y permite pisar valores con un archivo local
 * ./skillswap.properties o con propiedades de sistema (-Dclave=valor).
 */
public class Configuracion {

    private final Properties props = new Properties();

    public static Configuracion cargar() {
        Configuracion c = new Configuracion();
        try (InputStream in = Configuracion.class.getClassLoader().getResourceAsStream("skillswap.properties")) {
            if (in != null) {
                c.props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            }
            Path local = Path.of("skillswap.properties");
            if (Files.exists(local)) {
                try (Reader r = Files.newBufferedReader(local, StandardCharsets.UTF_8)) {
                    c.props.load(r);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        System.getProperties().forEach((k, v) -> {
            if (c.props.containsKey(k)) {
                c.props.setProperty(k.toString(), v.toString());
            }
        });
        return c;
    }

    public Configuracion con(String clave, String valor) {
        props.setProperty(clave, valor);
        return this;
    }

    public String texto(String clave, String porDefecto) {
        return props.getProperty(clave, porDefecto).strip();
    }

    public int entero(String clave, int porDefecto) {
        return Integer.parseInt(texto(clave, String.valueOf(porDefecto)));
    }

    public long largo(String clave, long porDefecto) {
        return Long.parseLong(texto(clave, String.valueOf(porDefecto)));
    }

    public double decimal(String clave, double porDefecto) {
        return Double.parseDouble(texto(clave, String.valueOf(porDefecto)));
    }

    public boolean booleano(String clave, boolean porDefecto) {
        return Boolean.parseBoolean(texto(clave, String.valueOf(porDefecto)));
    }
}
