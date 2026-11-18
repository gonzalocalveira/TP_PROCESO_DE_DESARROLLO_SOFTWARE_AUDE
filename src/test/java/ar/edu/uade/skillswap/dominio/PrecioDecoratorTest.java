package ar.edu.uade.skillswap.dominio;

import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.precio.CalculadorPrecio;
import ar.edu.uade.skillswap.dominio.precio.ComisionDecorator;
import ar.edu.uade.skillswap.dominio.precio.PoliticaPrecios;
import ar.edu.uade.skillswap.dominio.precio.PrecioBase;
import ar.edu.uade.skillswap.dominio.precio.PromocionDecorator;
import ar.edu.uade.skillswap.dominio.publicacion.Promocion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.soporte.Fixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** PATRÓN DECORATOR: promociones acumulables y comisión, sin total negativo. */
class PrecioDecoratorTest {

    @Test
    @DisplayName("Las promociones se acumulan envolviendo el precio base")
    void promocionesAcumulables() {
        CalculadorPrecio precio = PromocionDecorator.montoFijo(
                PromocionDecorator.porcentual(new PrecioBase(SkillCoins.de(200)), "Lanzamiento", 10), "Cupón", SkillCoins.de(30));
        assertEquals(SkillCoins.de(150), precio.calcular());   // 200 - 10% = 180 - 30 = 150
    }

    @Test
    @DisplayName("Regla 5: aunque las promociones superen el precio, el total nunca es negativo")
    void nuncaNegativo() {
        CalculadorPrecio precio = PromocionDecorator.montoFijo(
                PromocionDecorator.montoFijo(new PrecioBase(SkillCoins.de(100)), "A", SkillCoins.de(80)), "B", SkillCoins.de(80));
        assertEquals(SkillCoins.cero(), precio.calcular());
        assertEquals(SkillCoins.cero(), new ComisionDecorator(precio, 10).calcular());
    }

    @Test
    void comisionSeAplicaSobreElPrecioPromocionado() {
        ComisionDecorator total = new ComisionDecorator(PromocionDecorator.porcentual(new PrecioBase(SkillCoins.de(200)), "Promo", 50), 10);
        assertEquals(SkillCoins.de(110), total.calcular());
        assertEquals(SkillCoins.de(10), total.comision());
    }

    @Test
    void laPublicacionArmaLaCadenaConSusPromociones() {
        Publicacion p = Fixtures.publicada(140, 5);
        p.agregarPromocion(new Promocion("Lanzamiento", 20, SkillCoins.cero()));
        assertEquals(SkillCoins.de(112), p.precioFinal());
        assertEquals(SkillCoins.de(118), new PoliticaPrecios(5).precioParaCliente(p).calcular());
    }

    @Test
    void validaPorcentajesFueraDeRango() {
        assertThrows(ReglaNegocioException.class,
                () -> PromocionDecorator.porcentual(new PrecioBase(SkillCoins.de(100)), "X", 150));
        assertThrows(ReglaNegocioException.class, () -> new PrecioBase(SkillCoins.de(-1)));
    }
}
