package ar.edu.uade.skillswap.controllers;

import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import ar.edu.uade.skillswap.dominio.comun.SkillSwapException;
import ar.edu.uade.skillswap.infraestructura.config.SkillSwapContexto;
import ar.edu.uade.skillswap.model.Models.Contratacion;
import ar.edu.uade.skillswap.model.Models.EstadoContratacion;
import ar.edu.uade.skillswap.model.Models.EstadoPublicacion;
import ar.edu.uade.skillswap.model.Models.FiltrosBusqueda;
import ar.edu.uade.skillswap.model.Models.Modalidad;
import ar.edu.uade.skillswap.model.Models.Publicacion;
import ar.edu.uade.skillswap.soporte.ContextoPrueba;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El contrato UiControllers del kit, implementado con los casos de uso reales y los datos de demo. */
class SkillSwapControllersTest {

    private static final String MATEO = "user-mateo";
    private static SkillSwapContexto ctx;
    private static UiControllers ui;

    @BeforeAll
    static void iniciar() {
        ctx = ContextoPrueba.nuevo(true);
        ui = ctx.controllers();
    }

    @AfterAll
    static void cerrar() {
        ctx.close();
    }

    @Test
    void perfilYDatosDeDemo() {
        var perfil = ui.perfiles().verPerfil(MATEO);
        assertEquals("Mateo Silva", perfil.nombre());
        assertEquals(52, perfil.proyectosCompletados());
        assertEquals(List.of("Todas", "Tecnología", "Diseño", "Creatividad", "Negocios", "Productividad", "Idiomas"),
                ui.busqueda().categorias());
    }

    @Test
    void recomiendaTresQueNoSonPropias() {
        List<Publicacion> rec = ui.busqueda().recomendar(MATEO);
        assertEquals(3, rec.size());
        assertTrue(rec.stream().noneMatch(p -> p.proveedor().equals("Mateo Silva")));
    }

    @Test
    void buscarConLosFiltrosDeLaUi() {
        List<Publicacion> java = ui.busqueda().buscar(new FiltrosBusqueda("Java", "Todas", null, 0, "Relevancia"));
        assertTrue(java.stream().anyMatch(p -> p.titulo().contains("Arquitectura")));
        List<Publicacion> baratas = ui.busqueda().buscar(new FiltrosBusqueda("", "Todas", null, 0, "Menor precio"));
        assertTrue(baratas.get(0).titulo().contains("katas"), "la más barata es el desafío de 90 SC");
        assertTrue(baratas.stream().allMatch(p -> p.estado() == EstadoPublicacion.PUBLICADA));
    }

    @Test
    @DisplayName("Flujo completo desde la UI: solicitar, pagar (avanzar), y el saldo baja")
    void flujoCompletoDesdeLaUi() {
        Publicacion fotos = ui.busqueda().buscar(new FiltrosBusqueda("Fotografía", "Todas", null, 0, "Relevancia")).get(0);
        int saldoAntes = ui.wallet().consultarSaldo(MATEO);
        Contratacion c = ui.contrataciones().solicitar(MATEO, fotos.id(), "Quiero profundizar y practicar");
        assertEquals(EstadoContratacion.ACEPTADA, c.estado(), "la publicación de demo acepta automáticamente");
        Contratacion pagada = ui.contrataciones().avanzar(MATEO, c.id());
        assertEquals(EstadoContratacion.CONFIRMADA, pagada.estado());
        assertEquals(saldoAntes - fotos.precio(), ui.wallet().consultarSaldo(MATEO), "se cobra el precio mostrado");
        assertEquals(EstadoContratacion.EN_CURSO, ui.contrataciones().avanzar(MATEO, c.id()).estado());
        assertEquals(EstadoContratacion.COMPLETADA, ui.contrataciones().avanzar(MATEO, c.id()).estado());
        assertThrows(SkillSwapException.class, () -> ui.contrataciones().avanzar(MATEO, c.id()));
    }

    @Test
    @DisplayName("Mateo como proveedor: avanzar acepta y cancelar rechaza la solicitud de Lucía")
    void mateoComoProveedor() {
        List<Contratacion> propias = ui.contrataciones().listarContrataciones(MATEO);
        Contratacion deLucia = propias.stream()
                .filter(c -> c.contraparte().equals("Lucía Fernández") && c.estado() == EstadoContratacion.PENDIENTE)
                .findFirst().orElseThrow();
        assertEquals(EstadoContratacion.ACEPTADA, ui.contrataciones().avanzar(MATEO, deLucia.id()).estado());
        SkillSwapException e = assertThrows(SkillSwapException.class, () -> ui.contrataciones().avanzar(MATEO, deLucia.id()));
        assertEquals("Solo el cliente puede pagar.", e.getMessage());
        assertEquals(EstadoContratacion.RECHAZADA, ui.contrataciones().cancelar(MATEO, deLucia.id()).estado());
    }

    @Test
    void misPublicacionesYCambioDeEstado() {
        Publicacion nueva = ui.publicaciones().crearBorrador(MATEO, "Taller de SQL", "Consultas y modelado",
                "Tecnología", Modalidad.REMOTA, 60, 150);
        assertEquals(EstadoPublicacion.BORRADOR, nueva.estado());
        assertEquals(158, nueva.precio(), "150 + 5% de comisión");
        assertEquals(EstadoPublicacion.PUBLICADA, ui.publicaciones().cambiarEstado(MATEO, nueva.id(), EstadoPublicacion.PUBLICADA).estado());
        assertEquals(EstadoPublicacion.PAUSADA, ui.publicaciones().cambiarEstado(MATEO, nueva.id(), EstadoPublicacion.PAUSADA).estado());
        assertTrue(ui.publicaciones().listarPropias(MATEO).stream().anyMatch(p -> p.id().equals(nueva.id())));
        assertThrows(PermisoDenegadoException.class,
                () -> ui.publicaciones().cambiarEstado("user-lucia", nueva.id(), EstadoPublicacion.PUBLICADA));
        assertThrows(SkillSwapException.class,
                () -> ui.publicaciones().crearBorrador(MATEO, " ", "x", "Tecnología", Modalidad.REMOTA, 60, 150));
    }

    @Test
    void walletYNotificaciones() {
        int saldo = ui.wallet().consultarSaldo(MATEO);
        assertEquals(saldo + 500, ui.wallet().acreditarDemo(MATEO, 500));
        assertEquals("Créditos promocionales", ui.wallet().listarMovimientos(MATEO).get(0).concepto());
        assertEquals(500, ui.wallet().listarMovimientos(MATEO).get(0).importe());
        assertThrows(SkillSwapException.class, () -> ui.wallet().acreditarDemo(MATEO, -10));

        assertTrue(ui.notificaciones().listarNotificaciones(MATEO).stream().anyMatch(n -> !n.leida()));
        ui.notificaciones().marcarTodasLeidas(MATEO);
        assertTrue(ui.notificaciones().listarNotificaciones(MATEO).stream().allMatch(n -> n.leida()));
        assertFalse(ui.notificaciones().listarNotificaciones(MATEO).isEmpty());
    }

    @Test
    void usuarioInvalidoEsRechazado() {
        assertThrows(PermisoDenegadoException.class, () -> ui.wallet().consultarSaldo("user-desconocido"));
        assertThrows(PermisoDenegadoException.class, () -> ui.perfiles().verPerfil("user-desconocido"));
    }
}
