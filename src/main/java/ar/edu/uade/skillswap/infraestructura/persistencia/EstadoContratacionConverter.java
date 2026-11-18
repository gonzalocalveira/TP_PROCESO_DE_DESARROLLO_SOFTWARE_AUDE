package ar.edu.uade.skillswap.infraestructura.persistencia;

import ar.edu.uade.skillswap.dominio.contratacion.estado.EstadoContratacion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** PATRÓN STATE + persistencia: nombre del estado <-> objeto estado concreto. */
@Converter(autoApply = true)
public class EstadoContratacionConverter implements AttributeConverter<EstadoContratacion, String> {

    @Override
    public String convertToDatabaseColumn(EstadoContratacion estado) {
        return estado == null ? null : estado.nombre();
    }

    @Override
    public EstadoContratacion convertToEntityAttribute(String nombre) {
        return nombre == null ? null : EstadoContratacion.porNombre(nombre);
    }
}
