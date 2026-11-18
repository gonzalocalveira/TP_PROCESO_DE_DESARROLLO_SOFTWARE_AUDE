package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.comandos.SolicitarCmd;
import ar.edu.uade.skillswap.dominio.comun.ConflictoConcurrenciaException;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.infraestructura.config.SkillSwapContexto;
import ar.edu.uade.skillswap.soporte.ContextoPrueba;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** Bloqueo optimista (@Version): no hay sobreventa del cupo con usuarios simultáneos. */
class ConcurrenciaTest {

    private SkillSwapContexto ctx;
    private String pub;

    @BeforeEach
    void preparar() {
        ctx = ContextoPrueba.nuevo(false);
        ContextoPrueba.perfil(ctx, "user-profe", 0);
        pub = ContextoPrueba.publicacion(ctx, "user-profe", "Último lugar", "Tecnología", 100, 1, 3);
        for (int i = 0; i < 8; i++) {
            ContextoPrueba.perfil(ctx, "user-c" + i, 1000);
        }
    }

    @AfterEach
    void cerrar() {
        ctx.close();
    }

    @Test
    @DisplayName("Dos transacciones que leen la misma versión: la segunda falla con conflicto")
    void dosTransaccionesSobreLaMismaVersion() throws Exception {
        CyclicBarrier ambasLeyeron = new CyclicBarrier(2);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<?>> tareas = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            int n = i;
            tareas.add(pool.submit(() -> ctx.tx().ejecutarAccion(() -> {
                Publicacion p = ctx.publicaciones().obtener(pub);
                await(ambasLeyeron);                      // las dos tienen version = 1 en memoria
                p.reservar(p.getFranjas().get(n).getId(), LocalDateTime.now());
            })));
        }
        int ok = 0;
        Throwable error = null;
        for (Future<?> f : tareas) {
            try {
                f.get(30, TimeUnit.SECONDS);
                ok++;
            } catch (java.util.concurrent.ExecutionException e) {
                error = e.getCause();
            }
        }
        pool.shutdown();
        assertEquals(1, ok);
        assertInstanceOf(ConflictoConcurrenciaException.class, error);
        assertEquals(1, ctx.tx().ejecutar(() -> ctx.publicaciones().obtener(pub).getCuposOcupados()));
    }

    @Test
    @DisplayName("8 clientes compiten por 1 cupo: exactamente uno lo consigue")
    void sinSobreventa() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CyclicBarrier largada = new CyclicBarrier(8);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger rechazos = new AtomicInteger();
        List<Future<?>> tareas = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            String cliente = "user-c" + i;
            tareas.add(pool.submit(() -> {
                await(largada);
                try {
                    ctx.gestionContratacion().solicitar(new SolicitarCmd(cliente, pub, null, "", ""));
                    exitos.incrementAndGet();
                } catch (ConflictoConcurrenciaException | ReglaNegocioException e) {
                    rechazos.incrementAndGet();
                }
            }));
        }
        for (Future<?> f : tareas) {
            f.get(60, TimeUnit.SECONDS);
        }
        pool.shutdown();
        assertEquals(1, exitos.get());
        assertEquals(7, rechazos.get());
        assertEquals(1, ctx.tx().ejecutar(() -> ctx.publicaciones().obtener(pub).getCuposOcupados()));
    }

    private static void await(CyclicBarrier b) {
        try {
            b.await(20, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
