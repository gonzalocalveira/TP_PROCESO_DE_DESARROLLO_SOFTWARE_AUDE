package ar.edu.uade.skillswap.dominio.perfil;

import ar.edu.uade.skillswap.dominio.comun.Validar;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Locale;
import java.util.Objects;

/** Value Object: una habilidad que el perfil domina (se persiste como colección embebida). */
@Embeddable
public class Habilidad {

    @Column(length = 80)
    private String nombre;
    @Column(length = 80)
    private String categoria;

    protected Habilidad() {
        // requerido por JPA
    }

    public Habilidad(String nombre, String categoria) {
        this.nombre = Validar.texto(nombre, "habilidad", 80);
        this.categoria = categoria == null ? "General" : categoria.strip();
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    /** Coincidencia sin distinguir mayúsculas (la usa la estrategia de afinidad). */
    public boolean coincideCon(String texto) {
        if (texto == null) {
            return false;
        }
        String t = texto.toLowerCase(Locale.ROOT);
        String n = nombre.toLowerCase(Locale.ROOT);
        return t.contains(n) || n.contains(t);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Habilidad h && nombre.equalsIgnoreCase(h.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre.toLowerCase(Locale.ROOT));
    }
}
