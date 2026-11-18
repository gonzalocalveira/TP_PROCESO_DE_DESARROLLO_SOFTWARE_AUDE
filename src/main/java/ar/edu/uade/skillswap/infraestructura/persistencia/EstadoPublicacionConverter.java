package ar.edu.uade.skillswap.infraestructura.persistencia;

import ar.edu.uade.skillswap.dominio.publicacion.estado.EstadoPublicacion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * PATRÓN STATE + persistencia: en la base se guarda el NOMBRE del estado (VARCHAR) y al leer se
 * reconstruye el objeto estado concreto. El dominio no sabe nada de columnas.
 */
@Converter(autoApply = true)
public class EstadoPublicacionConverter implements AttributeConverter<EstadoPublicacion, String> {

    @Override
    public String convertToDatabaseColumn(EstadoPublicacion estado) {
        return estado == null ? null : estado.nombre();
    }

    @Override
    public EstadoPublicacion convertToEntityAttribute(String nombre) {
        return nombre == null ? null : EstadoPublicacion.porNombre(nombre);
    }
}
