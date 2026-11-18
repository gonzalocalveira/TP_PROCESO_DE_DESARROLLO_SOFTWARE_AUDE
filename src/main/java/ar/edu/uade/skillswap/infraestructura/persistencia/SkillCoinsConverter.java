package ar.edu.uade.skillswap.infraestructura.persistencia;

import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

/** Mapea el Value Object SkillCoins a una columna DECIMAL. */
@Converter(autoApply = true)
public class SkillCoinsConverter implements AttributeConverter<SkillCoins, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(SkillCoins valor) {
        return valor == null ? null : valor.valor();
    }

    @Override
    public SkillCoins convertToEntityAttribute(BigDecimal columna) {
        return columna == null ? null : SkillCoins.de(columna);
    }
}
