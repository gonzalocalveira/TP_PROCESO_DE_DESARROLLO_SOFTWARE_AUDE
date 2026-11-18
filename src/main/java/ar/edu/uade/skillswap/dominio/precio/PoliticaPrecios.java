package ar.edu.uade.skillswap.dominio.precio;

import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;

/**
 * Servicio de dominio que arma la cadena de decoradores que ve el CLIENTE:
 *   ComisionDecorator( PromocionDecorator( ... PromocionDecorator( PrecioBase ) ) )
 * Publicacion arma las capas de promociones; acá se agrega la comisión configurable.
 * Es el único lugar que conoce el orden de las capas: así la UI y el cobro usan el mismo precio.
 */
public class PoliticaPrecios {

    private final double comisionPorcentaje;

    public PoliticaPrecios(double comisionPorcentaje) {
        this.comisionPorcentaje = comisionPorcentaje;
    }

    public ComisionDecorator precioParaCliente(Publicacion publicacion) {
        return new ComisionDecorator(publicacion.getCalculadorPrecio(), comisionPorcentaje);
    }

    public double getComisionPorcentaje() {
        return comisionPorcentaje;
    }
}
