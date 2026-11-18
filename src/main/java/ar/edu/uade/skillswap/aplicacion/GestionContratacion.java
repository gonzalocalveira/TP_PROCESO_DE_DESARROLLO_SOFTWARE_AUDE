package ar.edu.uade.skillswap.aplicacion;

import ar.edu.uade.skillswap.aplicacion.comandos.SolicitarCmd;
import ar.edu.uade.skillswap.aplicacion.puertos.Comprobante;
import ar.edu.uade.skillswap.aplicacion.puertos.ContratacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.PerfilRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.ProcesadorPagos;
import ar.edu.uade.skillswap.aplicacion.puertos.PublicacionRepository;
import ar.edu.uade.skillswap.aplicacion.puertos.UnidadDeTrabajo;
import ar.edu.uade.skillswap.aplicacion.puertos.WalletRepository;
import ar.edu.uade.skillswap.aplicacion.vistas.ContratacionDetalle;
import ar.edu.uade.skillswap.dominio.cancelacion.PoliticaCancelacion;
import ar.edu.uade.skillswap.dominio.cancelacion.ReglasCancelacion;
import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import ar.edu.uade.skillswap.dominio.comun.SaldoInsuficienteException;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.contratacion.Contratacion;
import ar.edu.uade.skillswap.dominio.evento.EventoDominio;
import ar.edu.uade.skillswap.dominio.evento.PublicadorEventos;
import ar.edu.uade.skillswap.dominio.evento.TipoEvento;
import ar.edu.uade.skillswap.dominio.precio.ComisionDecorator;
import ar.edu.uade.skillswap.dominio.precio.PoliticaPrecios;
import ar.edu.uade.skillswap.dominio.publicacion.FranjaDisponibilidad;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.wallet.Wallet;

import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Caso de uso central: ciclo de vida de una contratación.
 *
 * Patrones que coordina:
 *  - STATE:     las transiciones las valida el estado de la Contratacion (acá no hay ifs de estado).
 *  - STRATEGY:  al cancelar, ReglasCancelacion elige la PoliticaCancelacion y se le delega el cálculo.
 *  - DECORATOR: el total a cobrar sale de la cadena Comision(Promociones(PrecioBase)).
 *  - OBSERVER:  después del commit se publican eventos; los observers notifican por los canales.
 *  - ADAPTER:   el cobro pasa por ProcesadorPagos (adapter de la pasarela externa).
 *
 * Cada operación corre en UNA transacción (UnidadDeTrabajo): débito al cliente y crédito al
 * proveedor se confirman juntos o no se confirma ninguno.
 */
public class GestionContratacion {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final ContratacionRepository contrataciones;
    private final PublicacionRepository publicaciones;
    private final WalletRepository wallets;
    private final PerfilRepository perfiles;
    private final ProcesadorPagos procesadorPagos;
    private final PoliticaPrecios politicaPrecios;
    private final ReglasCancelacion reglasCancelacion;
    private final UnidadDeTrabajo tx;
    private final PublicadorEventos eventos;
    private final EnsambladorVistas vistas;
    private final Clock reloj;

    public GestionContratacion(ContratacionRepository contrataciones, PublicacionRepository publicaciones,
                               WalletRepository wallets, PerfilRepository perfiles, ProcesadorPagos procesadorPagos,
                               PoliticaPrecios politicaPrecios, ReglasCancelacion reglasCancelacion,
                               UnidadDeTrabajo tx, PublicadorEventos eventos, EnsambladorVistas vistas, Clock reloj) {
        this.contrataciones = contrataciones;
        this.publicaciones = publicaciones;
        this.wallets = wallets;
        this.perfiles = perfiles;
        this.procesadorPagos = procesadorPagos;
        this.politicaPrecios = politicaPrecios;
        this.reglasCancelacion = reglasCancelacion;
        this.tx = tx;
        this.eventos = eventos;
        this.vistas = vistas;
        this.reloj = reloj;
    }

    // ================================================================== Solicitud

    public ContratacionDetalle solicitar(SolicitarCmd cmd) {
        List<EventoDominio> pendientes = new ArrayList<>();
        ContratacionDetalle detalle = tx.ejecutar(() -> {
            LocalDateTime ahora = ahora();
            perfiles.obtener(cmd.clienteId()).exigirActivo();
            Publicacion pub = publicaciones.obtener(cmd.publicacionId());
            Contratacion.validarSolicitud(pub, cmd.clienteId());           // reglas 1 y 2

            // regla 3: no reservar franja ocupada ni superar el cupo (validado por la Publicacion)
            FranjaDisponibilidad franja = cmd.franjaId() == null
                    ? pub.reservarPrimeraDisponible(ahora)
                    : pub.reservar(cmd.franjaId(), ahora);

            // PATRÓN DECORATOR: total = Comision(Promociones(PrecioBase))
            ComisionDecorator precio = politicaPrecios.precioParaCliente(pub);
            Contratacion c = Contratacion.solicitar(pub, cmd.clienteId(), franja, cmd.mensaje(), cmd.objetivo(),
                    precio.calcular(), precio.comision(), ahora);
            pendientes.add(evento(TipoEvento.SOLICITUD_RECIBIDA, c, List.of(c.getProveedorId()),
                    "Nueva solicitud", "Recibiste una solicitud para “" + c.getTitulo() + "” el " + FECHA.format(c.getFechaSesion()) + "."));

            if (pub.isAceptacionAutomatica()) {
                c.aceptar(Contratacion.SISTEMA, ahora);                    // STATE: PENDIENTE -> ACEPTADA
                pendientes.add(evento(TipoEvento.SOLICITUD_ACEPTADA, c, List.of(c.getClienteId()),
                        "Reserva aceptada", "“" + c.getTitulo() + "” se aceptó automáticamente. Ya podés pagarla."));
            } else {
                pendientes.add(evento(TipoEvento.SOLICITUD_RECIBIDA, c, List.of(c.getClienteId()),
                        "Solicitud enviada", "Tu solicitud para “" + c.getTitulo() + "” fue enviada al proveedor."));
            }
            publicaciones.guardar(pub);     // incrementa la versión: protege el cupo (bloqueo optimista)
            contrataciones.guardar(c);
            return vistas.contratacion(c, cmd.clienteId());
        });
        eventos.publicarTodos(pendientes);  // PATRÓN OBSERVER: recién después del commit
        return detalle;
    }

    // ================================================================== Transiciones simples (STATE)

    public ContratacionDetalle aceptar(String actor, String contratacionId) {
        return transicion(actor, contratacionId, (c, ahora) -> c.aceptar(actor, ahora),
                c -> evento(TipoEvento.SOLICITUD_ACEPTADA, c, List.of(c.getClienteId()), "Solicitud aceptada",
                        "Tu solicitud para “" + c.getTitulo() + "” fue aceptada. Ya podés pagarla."));
    }

    public ContratacionDetalle rechazar(String actor, String contratacionId, String motivo) {
        return transicion(actor, contratacionId, (c, ahora) -> {
                    c.rechazar(actor, motivo, ahora);
                    liberarFranja(c);
                },
                c -> evento(TipoEvento.SOLICITUD_RECHAZADA, c, List.of(c.getClienteId()), "Solicitud rechazada",
                        "El proveedor rechazó “" + c.getTitulo() + "”."));
    }

    public ContratacionDetalle iniciar(String actor, String contratacionId) {
        return transicion(actor, contratacionId, (c, ahora) -> c.iniciar(actor, ahora),
                c -> evento(TipoEvento.SESION_INICIADA, c, List.of(c.getClienteId(), c.getProveedorId()), "Sesión en curso",
                        "Comenzó la sesión de “" + c.getTitulo() + "”."));
    }

    public ContratacionDetalle completar(String actor, String contratacionId) {
        return transicion(actor, contratacionId, (c, ahora) -> {
                    c.completar(actor, ahora);
                    perfiles.obtener(c.getProveedorId()).registrarProyectoCompletado();
                    perfiles.obtener(c.getClienteId()).registrarProyectoCompletado();
                },
                c -> evento(TipoEvento.CONTRATACION_COMPLETADA, c, List.of(c.getClienteId(), c.getProveedorId()),
                        "¡Sesión completada!", "“" + c.getTitulo() + "” finalizó. Ya podés calificar a tu contraparte."));
    }

    // ================================================================== Pago (una sola transacción)

    /**
     * Débito al cliente + crédito al proveedor + comisión a la plataforma, en la MISMA transacción.
     * El estado impide el doble pago (CONFIRMADA.pagar lanza excepción) y la versión de la wallet
     * impide que dos pagos simultáneos dejen un saldo inconsistente.
     */
    public ContratacionDetalle pagar(String actor, String contratacionId) {
        return transicion(actor, contratacionId, (c, ahora) -> {
                    c.pagar(actor, ahora);                                  // STATE: ACEPTADA -> CONFIRMADA
                    SkillCoins total = c.getTotal();
                    if (!total.esPositivo()) {
                        return;                                             // 100% bonificada: no hay cobro
                    }
                    SkillCoins neto = total.restar(c.getComision());
                    Wallet cliente = wallets.obtener(c.getClienteId());
                    Wallet proveedor = wallets.obtener(c.getProveedorId());
                    Wallet plataforma = wallets.obtener(Wallet.PLATAFORMA);

                    if (!cliente.puedeDebitar(total)) {                     // validar antes de ir a la pasarela
                        throw new SaldoInsuficienteException(cliente.getSaldo(), total);
                    }
                    Comprobante cobro = procesadorPagos.debitar(c.getClienteId(), total); // PATRÓN ADAPTER
                    Comprobante liquidacion = procesadorPagos.acreditar(c.getProveedorId(), neto);
                    cliente.debitar(total, "Pago: " + c.getTitulo(), cobro.codigo(), ahora);
                    proveedor.acreditar(neto, "Cobro: " + c.getTitulo(), liquidacion.codigo(), ahora);
                    if (c.getComision().esPositivo()) {
                        plataforma.acreditar(c.getComision(), "Comisión: " + c.getTitulo(), cobro.codigo(), ahora);
                    }
                    wallets.guardar(cliente);
                    wallets.guardar(proveedor);
                    wallets.guardar(plataforma);
                },
                c -> evento(TipoEvento.PAGO_CONFIRMADO, c, List.of(c.getClienteId(), c.getProveedorId()), "Pago confirmado",
                        "Se confirmó el pago de " + c.getTotal().aEntero() + " SkillCoins por “" + c.getTitulo() + "”."));
    }

    // ================================================================== Cancelación (STRATEGY)

    public ContratacionDetalle cancelar(String actor, String contratacionId, String motivo) {
        List<EventoDominio> pendientes = new ArrayList<>();
        ContratacionDetalle detalle = tx.ejecutar(() -> {
            LocalDateTime ahora = ahora();
            Contratacion c = contratacionDe(actor, contratacionId);
            boolean estabaPagada = c.getEstado().estaPagada();
            c.cancelar(actor, motivo, ahora);                                // STATE valida si se puede

            SkillCoins reintegro = SkillCoins.cero();
            String politica = "sin pago previo";
            if (estabaPagada) {
                // PATRÓN STRATEGY: se elige la política según anticipación y quién cancela,
                // y se le delega el cálculo. Este método no conoce los porcentajes.
                PoliticaCancelacion estrategia = reglasCancelacion.politicaPara(c, actor, ahora);
                reintegro = estrategia.calcularReintegro(c);
                politica = estrategia.nombre();
                devolver(c, reintegro, ahora);
            }
            c.registrarReintegro(reintegro);
            liberarFranja(c);
            contrataciones.guardar(c);

            String quien = c.esCliente(actor) ? "el cliente" : "el proveedor";
            pendientes.add(evento(TipoEvento.CONTRATACION_CANCELADA, c, List.of(c.getClienteId(), c.getProveedorId()),
                    "Contratación cancelada", "“" + c.getTitulo() + "” fue cancelada por " + quien + ". "
                            + politica + ": reintegro de " + reintegro.aEntero() + " SkillCoins."));
            return vistas.contratacion(c, actor);
        });
        eventos.publicarTodos(pendientes);
        return detalle;
    }

    /** Devuelve el reintegro al cliente; lo aportan proveedor y plataforma en proporción a lo que cobraron. */
    private void devolver(Contratacion c, SkillCoins reintegro, LocalDateTime ahora) {
        if (!reintegro.esPositivo()) {
            return;
        }
        SkillCoins total = c.getTotal();
        SkillCoins parteProveedor = SkillCoins.de(reintegro.valor()
                .multiply(total.restar(c.getComision()).valor())
                .divide(total.valor(), 0, RoundingMode.HALF_UP));
        SkillCoins partePlataforma = reintegro.restar(parteProveedor);

        Wallet cliente = wallets.obtener(c.getClienteId());
        Wallet proveedor = wallets.obtener(c.getProveedorId());
        Wallet plataforma = wallets.obtener(Wallet.PLATAFORMA);
        Comprobante comp = procesadorPagos.acreditar(c.getClienteId(), reintegro);
        if (parteProveedor.esPositivo()) {
            proveedor.debitar(parteProveedor, "Reintegro: " + c.getTitulo(), comp.codigo(), ahora);
        }
        if (partePlataforma.esPositivo()) {
            plataforma.debitar(partePlataforma, "Reintegro comisión: " + c.getTitulo(), comp.codigo(), ahora);
        }
        cliente.acreditar(reintegro, "Reintegro: " + c.getTitulo(), comp.codigo(), ahora);
        wallets.guardar(cliente);
        wallets.guardar(proveedor);
        wallets.guardar(plataforma);
    }

    // ================================================================== Reprogramación

    public ContratacionDetalle reprogramar(String actor, String contratacionId, String nuevaFranjaId) {
        return transicion(actor, contratacionId, (c, ahora) -> {
                    Publicacion pub = publicaciones.obtener(c.getPublicacionId());
                    pub.liberar(c.getFranjaId());
                    FranjaDisponibilidad nueva = pub.reservar(nuevaFranjaId, ahora);
                    c.reprogramar(nueva, actor, ahora);
                    publicaciones.guardar(pub);
                },
                c -> evento(TipoEvento.CONTRATACION_REPROGRAMADA, c, List.of(c.getClienteId(), c.getProveedorId()),
                        "Sesión reprogramada", "“" + c.getTitulo() + "” pasó al " + FECHA.format(c.getFechaSesion()) + "."));
    }

    // ================================================================== Consultas

    public List<ContratacionDetalle> listar(String userId) {
        return tx.ejecutar(() -> vistas.contrataciones(contrataciones.porUsuario(userId).stream()
                .sorted(Comparator.comparing(Contratacion::getFechaCreacion).reversed()).toList(), userId));
    }

    public ContratacionDetalle ver(String actor, String contratacionId) {
        return tx.ejecutar(() -> vistas.contratacion(contratacionDe(actor, contratacionId), actor));
    }

    // ================================================================== Auxiliares

    /** Plantilla común: cargar, validar participante, ejecutar acción, guardar y publicar el evento. */
    private ContratacionDetalle transicion(String actor, String contratacionId,
                                           BiConsumer<Contratacion, LocalDateTime> accion,
                                           Function<Contratacion, EventoDominio> evento) {
        List<EventoDominio> pendientes = new ArrayList<>();
        ContratacionDetalle detalle = tx.ejecutar(() -> {
            Contratacion c = contratacionDe(actor, contratacionId);
            accion.accept(c, ahora());
            contrataciones.guardar(c);
            pendientes.add(evento.apply(c));
            return vistas.contratacion(c, actor);
        });
        eventos.publicarTodos(pendientes);
        return detalle;
    }

    private Contratacion contratacionDe(String actor, String contratacionId) {
        Contratacion c = contrataciones.obtener(contratacionId);
        if (!c.esParticipante(actor)) {
            throw new PermisoDenegadoException("No participás de esta contratación.");
        }
        return c;
    }

    private void liberarFranja(Contratacion c) {
        Publicacion pub = publicaciones.obtener(c.getPublicacionId());
        pub.liberar(c.getFranjaId());
        publicaciones.guardar(pub);
    }

    private EventoDominio evento(TipoEvento tipo, Contratacion c, List<String> destinatarios, String titulo, String detalle) {
        return new EventoDominio(tipo, ahora(), c.getId(), destinatarios, titulo, detalle);
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(reloj);
    }
}
