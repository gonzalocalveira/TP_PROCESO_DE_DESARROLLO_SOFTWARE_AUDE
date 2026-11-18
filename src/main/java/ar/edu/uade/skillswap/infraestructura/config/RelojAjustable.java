package ar.edu.uade.skillswap.infraestructura.config;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Reloj del sistema con un desfase configurable. En uso normal el desfase es cero.
 * Sirve para cargar datos de demo "en el pasado" con los mismos casos de uso, y en los tests
 * para simular el paso del tiempo (ej: cancelar a 10 horas de la sesión).
 */
public class RelojAjustable extends Clock {

    private final Clock base;
    private volatile Duration desfase = Duration.ZERO;

    public RelojAjustable(Clock base) {
        this.base = base;
    }

    public static RelojAjustable delSistema() {
        return new RelojAjustable(Clock.systemDefaultZone());
    }

    public void desplazar(Duration desfase) {
        this.desfase = desfase;
    }

    public void restablecer() {
        this.desfase = Duration.ZERO;
    }

    @Override
    public ZoneId getZone() {
        return base.getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        RelojAjustable r = new RelojAjustable(base.withZone(zone));
        r.desfase = desfase;
        return r;
    }

    @Override
    public Instant instant() {
        return base.instant().plus(desfase);
    }
}
