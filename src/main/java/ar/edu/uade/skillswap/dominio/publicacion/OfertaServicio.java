package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

/** Servicio puntual (revisión de CV, portfolio, etc.). */
@Entity
@DiscriminatorValue("SERVICIO")
public class OfertaServicio extends Publicacion {

    protected OfertaServicio() {
    }

    public OfertaServicio(String autorId, String titulo, String descripcion, String categoria, Modalidad modalidad,
                          int duracionMin, int cupo, SkillCoins precioBase, LocalDateTime ahora) {
        super(autorId, titulo, descripcion, categoria, modalidad, duracionMin, cupo, precioBase, ahora);
    }

    @Override
    protected void validarEspecifico() {
        // sin reglas adicionales
    }

    @Override
    public String tipo() {
        return "Servicio";
    }
}
