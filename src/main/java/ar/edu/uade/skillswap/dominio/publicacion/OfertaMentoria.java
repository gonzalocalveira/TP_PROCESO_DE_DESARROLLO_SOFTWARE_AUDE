package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.Validar;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

/** Mentoría 1 a 1: sesiones de al menos 30 minutos. */
@Entity
@DiscriminatorValue("MENTORIA")
public class OfertaMentoria extends Publicacion {

    protected OfertaMentoria() {
    }

    public OfertaMentoria(String autorId, String titulo, String descripcion, String categoria, Modalidad modalidad,
                          int duracionMin, int cupo, SkillCoins precioBase, LocalDateTime ahora) {
        super(autorId, titulo, descripcion, categoria, modalidad, duracionMin, cupo, precioBase, ahora);
    }

    @Override
    protected void validarEspecifico() {
        Validar.que(getDuracionMin() >= 30, "Una mentoría debe durar al menos 30 minutos.");
    }

    @Override
    public String tipo() {
        return "Mentoría";
    }
}
