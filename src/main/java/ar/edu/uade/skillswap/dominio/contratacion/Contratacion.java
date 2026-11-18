package ar.edu.uade.skillswap.dominio.contratacion;

import ar.edu.uade.skillswap.dominio.comun.Ids;
import ar.edu.uade.skillswap.dominio.comun.PermisoDenegadoException;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.TransicionInvalidaException;
import ar.edu.uade.skillswap.dominio.contratacion.estado.EstadoContratacion;
import ar.edu.uade.skillswap.dominio.contratacion.estado.Pendiente;
import ar.edu.uade.skillswap.dominio.publicacion.FranjaDisponibilidad;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contratación entre un cliente y el autor (proveedor) de una publicación.
 *
 * PATRÓN STATE — Contexto.
 * Cada acción (aceptar, pagar, iniciar, completar, cancelar...) se delega en el estado actual.
 * El objeto estado valida si la transición es legal y fija el estado siguiente; la Contratacion
 * solo agrega lo común a todas las transiciones: validar QUIÉN puede hacerla y registrar el
 * historial inmutable (fecha, actor, motivo).
 *
 * La relación con Publicacion y Perfil se guarda por identificador (agregados independientes).
 */
@Entity
@Table(name = "contrataciones")
public class Contratacion {

    public static final String SISTEMA = "sistema";

    @Id
    @Column(length = 64)
    private String id;
    @Column(name = "publicacion_id", nullable = false, length = 64)
    private String publicacionId;
    @Column(name = "cliente_id", nullable = false, length = 64)
    private String clienteId;
    @Column(name = "proveedor_id", nullable = false, length = 64)
    private String proveedorId;
    @Column(nullable = false, length = 150)
    private String titulo;
    @Column(length = 1000)
    private String mensaje;
    @Column(length = 500)
    private String objetivo;
    @Column(nullable = false, precision = 12, scale = 0)
    private SkillCoins total;
    @Column(nullable = false, precision = 12, scale = 0)
    private SkillCoins comision;
    @Column(nullable = false, precision = 12, scale = 0)
    private SkillCoins reintegro = SkillCoins.cero();
    @Column(name = "franja_id", nullable = false, length = 64)
    private String franjaId;
    @Column(name = "fecha_sesion", nullable = false)
    private LocalDateTime fechaSesion;
    @Column(name = "fin_sesion", nullable = false)
    private LocalDateTime finSesion;
    @Column(nullable = false, length = 20)
    private EstadoContratacion estado;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contratacion_historial", joinColumns = @JoinColumn(name = "contratacion_id"))
    @OrderColumn(name = "orden")
    private List<HistorialEstado> historial = new ArrayList<>();
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Version
    private long version;

    protected Contratacion() {
        // requerido por JPA
    }

    private Contratacion(Publicacion publicacion, String clienteId, FranjaDisponibilidad franja, String mensaje,
                         String objetivo, SkillCoins total, SkillCoins comision, LocalDateTime ahora) {
        this.id = Ids.nuevo("con");
        this.publicacionId = publicacion.getId();
        this.clienteId = clienteId;
        this.proveedorId = publicacion.getAutorId();
        this.titulo = publicacion.getTitulo();
        this.mensaje = mensaje == null ? "" : mensaje.strip();
        this.objetivo = objetivo == null ? "" : objetivo.strip();
        this.total = total;
        this.comision = comision;
        this.franjaId = franja.getId();
        this.fechaSesion = franja.getInicio();
        this.finSesion = franja.getFin();
        this.fechaCreacion = ahora;
        this.estado = new Pendiente();
        historial.add(new HistorialEstado(ahora, clienteId, "Solicitud creada", null, estado.nombre()));
    }

    /**
     * Crea la solicitud. Regla 1: el autor no puede solicitar su propia publicación.
     * La franja ya debe haber sido reservada en la publicación (cupo/franja validados allí).
     */
    public static Contratacion solicitar(Publicacion publicacion, String clienteId, FranjaDisponibilidad franja,
                                         String mensaje, String objetivo, SkillCoins total, SkillCoins comision,
                                         LocalDateTime ahora) {
        validarSolicitud(publicacion, clienteId);
        return new Contratacion(publicacion, clienteId, franja, mensaje, objetivo, total, comision, ahora);
    }

    /** Reglas 1 y 2 del enunciado; se valida ANTES de reservar la franja. */
    public static void validarSolicitud(Publicacion publicacion, String clienteId) {
        if (publicacion.esDeAutor(clienteId)) {
            throw new ReglaNegocioException("No podés solicitar tu propia publicación.");
        }
        if (!publicacion.aceptaSolicitudes()) {
            throw new ReglaNegocioException("La publicación no está publicada; no admite solicitudes.");
        }
    }

    // ------------------------------------------------------------------ PATRÓN STATE: delegación

    public void aceptar(String actor, LocalDateTime ahora) {
        exigirProveedor(actor, "aceptar la solicitud");
        transicionar(actor, "Solicitud aceptada", ahora, () -> estado.aceptar(this));
    }

    public void rechazar(String actor, String motivo, LocalDateTime ahora) {
        exigirProveedor(actor, "rechazar la solicitud");
        transicionar(actor, motivoO(motivo, "Solicitud rechazada"), ahora, () -> estado.rechazar(this, motivo));
    }

    public void pagar(String actor, LocalDateTime ahora) {
        exigirCliente(actor, "pagar");
        transicionar(actor, "Pago confirmado", ahora, () -> estado.pagar(this));
    }

    public void iniciar(String actor, LocalDateTime ahora) {
        exigirParticipante(actor);
        transicionar(actor, "Sesión iniciada", ahora, () -> estado.iniciar(this));
    }

    public void completar(String actor, LocalDateTime ahora) {
        exigirParticipante(actor);
        transicionar(actor, "Sesión completada", ahora, () -> estado.completar(this));
    }

    public void cancelar(String actor, String motivo, LocalDateTime ahora) {
        exigirParticipante(actor);
        transicionar(actor, motivoO(motivo, "Cancelada"), ahora, () -> estado.cancelar(this, motivo));
    }

    /** Cambia la franja (solo antes de iniciar). La franja nueva ya fue reservada en la publicación. */
    public void reprogramar(FranjaDisponibilidad nueva, String actor, LocalDateTime ahora) {
        exigirParticipante(actor);
        if (!estado.permiteReprogramar()) {
            throw new TransicionInvalidaException(estado.nombre(), "reprogramar");
        }
        this.franjaId = nueva.getId();
        this.fechaSesion = nueva.getInicio();
        this.finSesion = nueva.getFin();
        registrarHistorial(actor, "Reprogramada para " + nueva.getInicio(), ahora, estado.nombre());
    }

    /** Lo invocan exclusivamente los objetos estado. */
    public void cambiarEstado(EstadoContratacion nuevo) {
        this.estado = nuevo;
    }

    private void transicionar(String actor, String motivo, LocalDateTime ahora, Runnable accionDelEstado) {
        String anterior = estado.nombre();
        accionDelEstado.run();                      // el estado valida y cambia (o lanza excepción)
        registrarHistorial(actor, motivo, ahora, anterior);
    }

    private void registrarHistorial(String actor, String motivo, LocalDateTime ahora, String anterior) {
        historial.add(new HistorialEstado(ahora, actor, motivo, anterior, estado.nombre()));
    }

    // ------------------------------------------------------------------ Permisos (quién puede qué)

    private void exigirProveedor(String actor, String accion) {
        if (!proveedorId.equals(actor) && !SISTEMA.equals(actor)) {
            throw new PermisoDenegadoException("Solo el proveedor puede " + accion + ".");
        }
    }

    private void exigirCliente(String actor, String accion) {
        if (!clienteId.equals(actor)) {
            throw new PermisoDenegadoException("Solo el cliente puede " + accion + ".");
        }
    }

    private void exigirParticipante(String actor) {
        if (!esParticipante(actor) && !SISTEMA.equals(actor)) {
            throw new PermisoDenegadoException("No participás de esta contratación.");
        }
    }

    public boolean esParticipante(String userId) {
        return clienteId.equals(userId) || proveedorId.equals(userId);
    }

    public boolean esCliente(String userId) {
        return clienteId.equals(userId);
    }

    public boolean esProveedor(String userId) {
        return proveedorId.equals(userId);
    }

    public String contraparteDe(String userId) {
        if (esCliente(userId)) {
            return proveedorId;
        }
        if (esProveedor(userId)) {
            return clienteId;
        }
        throw new PermisoDenegadoException("No participás de esta contratación.");
    }

    public long horasHastaSesion(LocalDateTime ahora) {
        return Duration.between(ahora, fechaSesion).toHours();
    }

    public void registrarReintegro(SkillCoins monto) {
        this.reintegro = monto;
    }

    private static String motivoO(String motivo, String porDefecto) {
        return motivo == null || motivo.isBlank() ? porDefecto : motivo;
    }

    public String getId() {
        return id;
    }

    public String getPublicacionId() {
        return publicacionId;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public SkillCoins getTotal() {
        return total;
    }

    public SkillCoins getComision() {
        return comision;
    }

    public SkillCoins getReintegro() {
        return reintegro;
    }

    public String getFranjaId() {
        return franjaId;
    }

    public LocalDateTime getFechaSesion() {
        return fechaSesion;
    }

    public LocalDateTime getFinSesion() {
        return finSesion;
    }

    public EstadoContratacion getEstado() {
        return estado;
    }

    public List<HistorialEstado> getHistorial() {
        return Collections.unmodifiableList(historial);
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public long getVersion() {
        return version;
    }
}
