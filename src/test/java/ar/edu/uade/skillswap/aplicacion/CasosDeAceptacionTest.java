package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.comandos.AccionPublicacion;
import ar.edu.uade.skillswap.aplicacion.comandos.CalificarCmd;
import ar.edu.uade.skillswap.aplicacion.comandos.SolicitarCmd;
import ar.edu.uade.skillswap.aplicacion.puertos.FiltroBusqueda;
import ar.edu.uade.skillswap.aplicacion.puertos.OrdenBusqueda;
import ar.edu.uade.skillswap.aplicacion.puertos.Pagina;
import ar.edu.uade.skillswap.aplicacion.vistas.ContratacionDetalle;
import ar.edu.uade.skillswap.aplicacion.vistas.PublicacionDetalle;
import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.SaldoInsuficienteException;
import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.contratacion.HistorialEstado;
import ar.edu.uade.skillswap.dominio.wallet.Wallet;
import ar.edu.uade.skillswap.infraestructura.config.SkillSwapContexto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static ar.edu.uade.skillswap.soporte.ContextoPrueba.irA;
import static ar.edu.uade.skillswap.soporte.ContextoPrueba.perfil;
import static ar.edu.uade.skillswap.soporte.ContextoPrueba.publicacion;
import static ar.edu.uade.skillswap.soporte.ContextoPrueba.saldo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Casos de aceptación del enunciado, contra el sistema completo (casos de uso + Hibernate + H2).
 * Incluye casos NEGATIVOS: cada regla se prueba también cuando se intenta violar.
 */
class CasosDeAceptacionTest {

    private static final String PROFE = "user-profe";
    private static final String ALUMNO = "user-alumno";
    private static final String OTRO = "user-otro";

    private SkillSwapContexto ctx;
    private String pub;

    @BeforeEach
    void preparar() {
        ctx = ar.edu.uade.skillswap.soporte.ContextoPrueba.nuevo(false);
        perfil(ctx, PROFE, 0, "Java");
        perfil(ctx, ALUMNO, 1000, "Java");
        perfil(ctx, OTRO, 1000);
        pub = publicacion(ctx, PROFE, "Java para principiantes", "Tecnología", 200, 2, 5, "Java", "POO");
    }

    @AfterEach
    void cerrar() {
        ctx.close();
    }

    private ContratacionDetalle solicitar(String cliente) {
        return ctx.gestionContratacion().solicitar(new SolicitarCmd(cliente, pub, null, "Hola", "Aprender"));
    }

    private String aceptadaPor(String cliente) {
        String id = solicitar(cliente).contratacion().getId();
        ctx.gestionContratacion().aceptar(PROFE, id);
        return id;
    }

    // ------------------------------------------------------------------ reglas 1 a 3

    @Test
    @DisplayName("Regla 1: el autor no puede solicitar su propia publicación")
    void autorNoSolicitaLoPropio() {
        assertThrows(ReglaNegocioException.class, () -> solicitar(PROFE));
    }

    @Test
    @DisplayName("Regla 2: una publicación pausada no recibe solicitudes")
    void pausadaNoRecibeSolicitudes() {
        ctx.gestionPublicacion().cambiarEstado(PROFE, pub, AccionPublicacion.PAUSAR, null);
        assertThrows(ReglaNegocioException.class, () -> solicitar(ALUMNO));
    }

    @Test
    @DisplayName("Regla 3: no se supera el cupo ni se reserva una franja ocupada")
    void cupoYFranjas() {
        ContratacionDetalle a = solicitar(ALUMNO);
        assertThrows(ReglaNegocioException.class, () -> ctx.gestionContratacion().solicitar(
                new SolicitarCmd(OTRO, pub, a.contratacion().getFranjaId(), "", "")), "franja ocupada");
        solicitar(OTRO);
        perfil(ctx, "user-tercero", 500);
        assertThrows(ReglaNegocioException.class, () -> solicitar("user-tercero"), "cupo 2 completo");
        assertEquals(2, ctx.tx().ejecutar(() -> ctx.publicaciones().obtener(pub).getCuposOcupados()));
    }

    @Test
    @DisplayName("Solo el autor modifica su publicación")
    void soloElAutorCambiaEstado() {
        assertThrows(PermisoDenegadoException.class,
                () -> ctx.gestionPublicacion().cambiarEstado(ALUMNO, pub, AccionPublicacion.PAUSAR, null));
    }

    // ------------------------------------------------------------------ pago en una sola transacción

    @Test
    @DisplayName("Regla 4: el pago debita al cliente y acredita proveedor + comisión en una transacción")
    void pagoAtomico() {
        String id = aceptadaPor(ALUMNO);
        ContratacionDetalle pagada = ctx.gestionContratacion().pagar(ALUMNO, id);
        assertEquals("CONFIRMADA", pagada.contratacion().getEstado().nombre());
        assertEquals(1000 - 210, saldo(ctx, ALUMNO));      // 200 + 5% comisión
        assertEquals(200, saldo(ctx, PROFE));
        assertEquals(10, saldo(ctx, Wallet.PLATAFORMA));
    }

    @Test
    @DisplayName("Saldo insuficiente: no se mueve ningún crédito y la contratación sigue ACEPTADA")
    void saldoInsuficienteHaceRollback() {
        perfil(ctx, "user-pobre", 50);
        String id = aceptadaPor("user-pobre");
        assertThrows(SaldoInsuficienteException.class, () -> ctx.gestionContratacion().pagar("user-pobre", id));
        assertEquals(50, saldo(ctx, "user-pobre"));
        assertEquals(0, saldo(ctx, PROFE));
        assertEquals("ACEPTADA", ctx.gestionContratacion().ver("user-pobre", id).contratacion().getEstado().nombre());
    }

    @Test
    @DisplayName("No hay doble pago")
    void doblePago() {
        String id = aceptadaPor(ALUMNO);
        ctx.gestionContratacion().pagar(ALUMNO, id);
        assertThrows(TransicionInvalidaException.class, () -> ctx.gestionContratacion().pagar(ALUMNO, id));
        assertEquals(790, saldo(ctx, ALUMNO));
    }

    // ------------------------------------------------------------------ cancelación (Strategy)

    @Test
    @DisplayName("Cancelación con más de 48 h: reintegro 100%")
    void cancelacionTotal() {
        String id = aceptadaPor(ALUMNO);
        ctx.gestionContratacion().pagar(ALUMNO, id);
        ContratacionDetalle c = ctx.gestionContratacion().cancelar(ALUMNO, id, "No llego");
        assertEquals(210, c.contratacion().getReintegro().aEntero());
        assertEquals(1000, saldo(ctx, ALUMNO));
        assertEquals(0, saldo(ctx, PROFE));
        assertEquals(0, saldo(ctx, Wallet.PLATAFORMA));
        assertEquals(0, ctx.tx().ejecutar(() -> ctx.publicaciones().obtener(pub).getCuposOcupados()), "libera el cupo");
    }

    @Test
    @DisplayName("Cancelación entre 24 y 48 h: reintegro 50%")
    void cancelacionParcial() {
        String id = aceptadaPor(ALUMNO);
        ctx.gestionContratacion().pagar(ALUMNO, id);
        LocalDateTime sesion = ctx.gestionContratacion().ver(ALUMNO, id).contratacion().getFechaSesion();
        irA(ctx, sesion.minusHours(30));
        ContratacionDetalle c = ctx.gestionContratacion().cancelar(ALUMNO, id, "Imprevisto");
        assertEquals(105, c.contratacion().getReintegro().aEntero());
        assertEquals(790 + 105, saldo(ctx, ALUMNO));
        assertEquals(100, saldo(ctx, PROFE));
    }

    @Test
    @DisplayName("Cancelación con menos de 24 h: sin reintegro; si cancela el proveedor: 100%")
    void cancelacionSinReintegroYPorProveedor() {
        String id = aceptadaPor(ALUMNO);
        ctx.gestionContratacion().pagar(ALUMNO, id);
        LocalDateTime sesion = ctx.gestionContratacion().ver(ALUMNO, id).contratacion().getFechaSesion();
        irA(ctx, sesion.minusHours(5));
        assertEquals(0, ctx.gestionContratacion().cancelar(ALUMNO, id, "Tarde").contratacion().getReintegro().aEntero());

        String otra = aceptadaPor(OTRO);
        ctx.gestionContratacion().pagar(OTRO, otra);
        assertEquals(210, ctx.gestionContratacion().cancelar(PROFE, otra, "Me enfermé").contratacion().getReintegro().aEntero());
        assertEquals(1000, saldo(ctx, OTRO));
    }

    // ------------------------------------------------------------------ historial, calificación, reputación

    @Test
    @DisplayName("El historial guarda fecha, actor y motivo de cada transición y persiste")
    void historialPersistido() {
        String id = aceptadaPor(ALUMNO);
        ctx.gestionContratacion().pagar(ALUMNO, id);
        ctx.gestionContratacion().cancelar(ALUMNO, id, "Cambio de planes");
        List<HistorialEstado> h = ctx.gestionContratacion().ver(ALUMNO, id).contratacion().getHistorial();
        assertEquals(List.of("PENDIENTE", "ACEPTADA", "CONFIRMADA", "CANCELADA"),
                h.stream().map(HistorialEstado::getEstadoNuevo).toList());
        assertEquals(PROFE, h.get(1).getActor());
        assertEquals("Cambio de planes", h.get(3).getMotivo());
    }

    @Test
    @DisplayName("Regla 6: solo participantes de una COMPLETADA califican, una vez, y actualiza la reputación")
    void calificaciones() {
        String id = aceptadaPor(ALUMNO);
        CalificarCmd cal = new CalificarCmd(ALUMNO, id, 4, "Bien");
        assertThrows(ReglaNegocioException.class, () -> ctx.gestionCalificacion().calificar(cal), "no completada");
        ctx.gestionContratacion().pagar(ALUMNO, id);
        ctx.gestionContratacion().iniciar(PROFE, id);
        ctx.gestionContratacion().completar(PROFE, id);

        ctx.gestionCalificacion().calificar(cal);
        assertThrows(ReglaNegocioException.class, () -> ctx.gestionCalificacion().calificar(cal), "una sola vez");
        assertThrows(PermisoDenegadoException.class,
                () -> ctx.gestionCalificacion().calificar(new CalificarCmd(OTRO, id, 5, "")), "no participó");
        assertEquals(4.0, ctx.gestionPerfil().ver(PROFE).getReputacion().valor());
        assertEquals(1, ctx.gestionPerfil().ver(PROFE).getReputacion().getCantidad());
        assertEquals(1, ctx.gestionPerfil().ver(PROFE).getProyectosCompletados());
    }

    // ------------------------------------------------------------------ baja lógica

    @Test
    @DisplayName("Baja lógica: el perfil queda inactivo, sus publicaciones se pausan y no aparece en búsquedas")
    void bajaLogica() {
        ctx.gestionPerfil().darDeBaja(PROFE, PROFE);
        assertFalse(ctx.gestionPerfil().ver(PROFE).isActivo(), "sigue existiendo, inactivo");
        assertThrows(ReglaNegocioException.class, () -> ctx.gestionPerfil().validarSesion(PROFE));
        assertEquals(0, ctx.busqueda().buscar(FiltroBusqueda.todo(10)).total());
        assertThrows(PermisoDenegadoException.class, () -> ctx.gestionPerfil().darDeBaja(ALUMNO, OTRO));
    }

    // ------------------------------------------------------------------ búsqueda y paginación

    @Test
    @DisplayName("Búsqueda paginada con filtros y orden")
    void busquedaPaginada() {
        for (int i = 1; i <= 24; i++) {
            publicacion(ctx, OTRO, "Curso " + i, i % 2 == 0 ? "Diseño" : "Negocios", 100 + i, 3, 2, "tag" + i);
        }
        Pagina<PublicacionDetalle> p0 = ctx.busqueda().buscar(new FiltroBusqueda(null, null, null, null,
                OrdenBusqueda.MENOR_PRECIO, 0, 10));
        assertEquals(25, p0.total());
        assertEquals(3, p0.totalPaginas());
        assertEquals(10, p0.contenido().size());
        assertEquals(101, p0.contenido().get(0).publicacion().getPrecioBase().aEntero());
        Pagina<PublicacionDetalle> p2 = ctx.busqueda().buscar(new FiltroBusqueda(null, null, null, null,
                OrdenBusqueda.MENOR_PRECIO, 2, 10));
        assertEquals(5, p2.contenido().size());
        assertFalse(p2.hayMas());

        assertEquals(12, ctx.busqueda().buscar(new FiltroBusqueda(null, "Diseño", null, null, null, 0, 50)).total());
        assertEquals(1, ctx.busqueda().buscar(new FiltroBusqueda("tag7", null, null, null, null, 0, 50)).total(), "por etiqueta");
        assertEquals(1, ctx.busqueda().buscar(new FiltroBusqueda("poo", null, null, null, null, 0, 50)).total(), "sin mayúsculas");
        assertEquals(1, ctx.busqueda().buscar(new FiltroBusqueda("profe", null, null, null, null, 0, 50)).total(), "por autor");
        assertEquals(2, ctx.busqueda().buscar(new FiltroBusqueda(null, null, null, 102, null, 0, 50)).total(), "precio máximo");
        assertThrows(ReglaNegocioException.class, () -> new FiltroBusqueda(null, null, null, null, null, 0, 500));
    }

    // ------------------------------------------------------------------ Observer de punta a punta

    @Test
    @DisplayName("Cada paso genera notificaciones internas, email y push (Observer + Adapter)")
    void notificaciones() {
        String id = aceptadaPor(ALUMNO);
        ctx.gestionContratacion().pagar(ALUMNO, id);
        List<String> titulos = ctx.gestionNotificaciones().listar(ALUMNO).stream()
                .map(n -> n.getTitulo()).toList();
        assertTrue(titulos.contains("Solicitud aceptada"));
        assertTrue(titulos.contains("Pago confirmado"));
        assertTrue(ctx.emailSdk().enviados().stream().anyMatch(j -> j.contains("alumno@skillswap.demo")));
        assertFalse(ctx.pushSdk().enviados().isEmpty());
        assertTrue(ctx.auditoria().ultimos().stream().anyMatch(l -> l.contains("PAGO_CONFIRMADO")));

        String notif = ctx.gestionNotificaciones().listar(ALUMNO).get(0).getId();
        assertThrows(PermisoDenegadoException.class, () -> ctx.gestionNotificaciones().marcarLeida(PROFE, notif));
        ctx.gestionNotificaciones().marcarTodasLeidas(ALUMNO);
        assertTrue(ctx.gestionNotificaciones().listar(ALUMNO).stream().allMatch(n -> n.estaLeida()));
    }

    @Test
    @DisplayName("Strategy intercambiable en tiempo de ejecución")
    void cambiarEstrategiaDeRecomendacion() {
        String excel = publicacion(ctx, OTRO, "Excel", "Productividad", 50, 5, 2, "Excel");
        assertEquals(pub, ctx.recomendaciones().recomendar(ALUMNO, 1).get(0).publicacion().getId(), "afinidad: Java");
        perfil(ctx, "user-x1", 500);
        perfil(ctx, "user-x2", 500);
        ctx.gestionContratacion().solicitar(new SolicitarCmd("user-x1", excel, null, "", ""));
        ctx.gestionContratacion().solicitar(new SolicitarCmd("user-x2", excel, null, "", ""));
        ctx.recomendaciones().setEstrategia(
                new ar.edu.uade.skillswap.dominio.recomendacion.PopularidadReciente(ctx.reloj(), 30));
        assertEquals(excel, ctx.recomendaciones().recomendar(ALUMNO, 1).get(0).publicacion().getId(), "popularidad");
        assertTrue(ctx.recomendaciones().recomendar(PROFE, 10).stream()
                .noneMatch(d -> d.publicacion().getAutorId().equals(PROFE)), "no recomienda lo propio");
    }

    @Test
    @DisplayName("Usuario desconocido para el proveedor de identidad: sesión inválida")
    void sesionInvalida() {
        assertThrows(PermisoDenegadoException.class, () -> ctx.gestionPerfil().validarSesion("user-hacker"));
        assertThrows(PermisoDenegadoException.class, () -> ctx.gestionPerfil().validarSesion(null));
    }

    @Test
    @DisplayName("Un tercero no puede ver ni operar una contratación ajena")
    void contratacionAjena() {
        String id = solicitar(ALUMNO).contratacion().getId();
        assertThrows(PermisoDenegadoException.class, () -> ctx.gestionContratacion().ver(OTRO, id));
        assertThrows(PermisoDenegadoException.class, () -> ctx.gestionContratacion().cancelar(OTRO, id, ""));
        Contratacion c = ctx.gestionContratacion().ver(ALUMNO, id).contratacion();
        assertEquals("PENDIENTE", c.getEstado().nombre());
    }
}
