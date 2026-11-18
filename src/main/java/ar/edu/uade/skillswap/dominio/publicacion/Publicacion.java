package ar.edu.uade.skillswap.dominio.publicacion;

import ar.edu.uade.skillswap.dominio.comun.Ids;
import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.comun.Validar;
import ar.edu.uade.skillswap.dominio.precio.CalculadorPrecio;
import ar.edu.uade.skillswap.dominio.precio.PrecioBase;
import ar.edu.uade.skillswap.dominio.publicacion.estado.Borrador;
import ar.edu.uade.skillswap.dominio.publicacion.estado.EstadoPublicacion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Publicación (oferta de mentoría, servicio o desafío).
 *
 * PATRÓN STATE — Contexto.
 * La publicación NO tiene un "switch" por estado: delega cada acción (publicar, pausar, cerrar,
 * cancelar) en su objeto estado actual, y es el estado quien decide si la transición es válida
 * y cuál es el estado siguiente. Agregar un estado nuevo = agregar una clase, sin tocar esta.
 *
 * PATRÓN DECORATOR — Cliente.
 * getCalculadorPrecio() arma PrecioBase envuelto por una PromocionDecorator por cada promoción.
 *
 * Herencia: OfertaMentoria, OfertaServicio y Desafio redefinen tipo() y sus validaciones propias.
 * Concurrencia: el campo version activa el bloqueo optimista (evita sobreventa de cupo).
 */
@Entity
@Table(name = "publicaciones")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", length = 20)
public abstract class Publicacion {

    @Id
    @Column(length = 64)
    private String id;
    @Column(name = "autor_id", nullable = false, length = 64)
    private String autorId;
    @Column(nullable = false, length = 150)
    private String titulo;
    @Column(length = 2000)
    private String descripcion;
    @Column(nullable = false, length = 80)
    private String categoria;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidad modalidad;
    @Column(name = "duracion_min", nullable = false)
    private int duracionMin;
    @Column(nullable = false)
    private int cupo;
    @Column(name = "cupos_ocupados", nullable = false)
    private int cuposOcupados;
    @Column(name = "precio_base", nullable = false, precision = 12, scale = 0)
    private SkillCoins precioBase;
    @Column(nullable = false)
    private boolean destacada;
    @Column(name = "aceptacion_automatica", nullable = false)
    private boolean aceptacionAutomatica;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "publicacion_etiquetas", joinColumns = @JoinColumn(name = "publicacion_id"))
    @OrderColumn(name = "orden")
    @Column(name = "etiqueta", length = 60)
    private List<String> etiquetas = new ArrayList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "publicacion_promociones", joinColumns = @JoinColumn(name = "publicacion_id"))
    @OrderColumn(name = "orden")
    private List<Promocion> promociones = new ArrayList<>();
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "publicacion_id", nullable = false)
    @OrderColumn(name = "orden")
    private List<FranjaDisponibilidad> franjas = new ArrayList<>();
    @Column(nullable = false, length = 20)
    private EstadoPublicacion estado;
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    @Column(name = "fecha_publicacion")
    private LocalDateTime fechaPublicacion;
    @Version
    private long version;

    protected Publicacion() {
        // requerido por JPA
    }

    protected Publicacion(String autorId, String titulo, String descripcion, String categoria,
                          Modalidad modalidad, int duracionMin, int cupo, SkillCoins precioBase,
                          LocalDateTime ahora) {
        this.id = Ids.nuevo("pub");
        this.autorId = Validar.texto(autorId, "autor", 64);
        this.titulo = Validar.texto(titulo, "título", 150);
        this.descripcion = descripcion == null ? "" : descripcion.strip();
        this.categoria = Validar.texto(categoria, "categoría", 80);
        Validar.que(modalidad != null, "La modalidad es obligatoria.");
        this.modalidad = modalidad;
        Validar.que(duracionMin >= 15 && duracionMin <= 480, "La duración debe estar entre 15 y 480 minutos.");
        this.duracionMin = duracionMin;
        Validar.que(cupo >= 1, "El cupo debe ser al menos 1.");
        this.cupo = cupo;
        Validar.que(precioBase != null && !precioBase.esNegativo(), "El precio no puede ser negativo.");
        this.precioBase = precioBase;
        this.fechaCreacion = ahora;
        this.estado = new Borrador(); // toda publicación nace en BORRADOR
    }

    // ------------------------------------------------------------------ PATRÓN STATE: delegación

    public void publicar(LocalDateTime ahora) {
        estado.publicar(this);
        if (fechaPublicacion == null) {
            fechaPublicacion = ahora;
        }
    }

    public void pausar() {
        estado.pausar(this);
    }

    public void cerrar() {
        estado.cerrar(this);
    }

    public void cancelar(String motivo) {
        estado.cancelar(this, motivo);
    }

    /** Lo invocan exclusivamente los objetos estado para pasar al estado siguiente. */
    public void cambiarEstado(EstadoPublicacion nuevo) {
        this.estado = nuevo;
    }

    public boolean aceptaSolicitudes() {
        return estado.aceptaSolicitudes();
    }

    // ------------------------------------------------------------------ Template: validaciones de publicación

    /**
     * Reglas comunes para poder publicar. Lo llama el estado Borrador/Pausada antes de pasar a Publicada.
     * Cada subclase agrega sus reglas en validarEspecifico() (polimorfismo).
     */
    public final void validarParaPublicar() {
        Validar.que(!descripcion.isBlank(), "Agregá una descripción antes de publicar.");
        Validar.que(precioBase.esPositivo(), "El precio debe ser mayor a cero para publicar.");
        Validar.que(!franjas.isEmpty(), "Agregá al menos una franja de disponibilidad antes de publicar.");
        validarEspecifico();
    }

    protected abstract void validarEspecifico();

    public abstract String tipo();

    // ------------------------------------------------------------------ Franjas y cupo

    public void agregarFranja(FranjaDisponibilidad f) {
        for (FranjaDisponibilidad existente : franjas) {
            if (existente.seSuperponeCon(f)) {
                throw new ReglaNegocioException("La franja se superpone con otra ya cargada.");
            }
        }
        franjas.add(f);
    }

    public boolean tieneCupo() {
        return cuposOcupados < cupo;
    }

    /**
     * Reserva una franja: valida estado, cupo y que la franja esté libre.
     * El incremento de cuposOcupados modifica la versión: si dos usuarios reservan el último
     * lugar a la vez, el segundo commit falla por bloqueo optimista (no hay sobreventa).
     */
    public FranjaDisponibilidad reservar(String franjaId, LocalDateTime ahora) {
        if (!aceptaSolicitudes()) {
            throw new ReglaNegocioException("La publicación no está publicada; no admite solicitudes.");
        }
        if (!tieneCupo()) {
            throw new ReglaNegocioException("La publicación no tiene cupo disponible.");
        }
        FranjaDisponibilidad franja = franjaPorId(franjaId)
                .orElseThrow(() -> new ReglaNegocioException("La franja indicada no pertenece a la publicación."));
        franja.ocupar(ahora);
        cuposOcupados++;
        return franja;
    }

    public FranjaDisponibilidad reservarPrimeraDisponible(LocalDateTime ahora) {
        FranjaDisponibilidad libre = proximaFranjaDisponible(ahora)
                .orElseThrow(() -> new ReglaNegocioException("No hay franjas disponibles para esta publicación."));
        return reservar(libre.getId(), ahora);
    }

    public void liberar(String franjaId) {
        franjaPorId(franjaId).ifPresent(f -> {
            if (f.isOcupada()) {
                f.liberar();
                cuposOcupados = Math.max(0, cuposOcupados - 1);
            }
        });
    }

    public Optional<FranjaDisponibilidad> proximaFranjaDisponible(LocalDateTime ahora) {
        return franjas.stream()
                .filter(f -> f.estaDisponible(ahora))
                .min(Comparator.comparing(FranjaDisponibilidad::getInicio));
    }

    public Optional<FranjaDisponibilidad> franjaPorId(String franjaId) {
        return franjas.stream().filter(f -> f.getId().equals(franjaId)).findFirst();
    }

    // ------------------------------------------------------------------ Precio (PATRÓN DECORATOR)

    public void agregarPromocion(Promocion p) {
        promociones.add(p);
    }

    /** Construye la cadena PrecioBase -> PromocionDecorator -> PromocionDecorator ... */
    public CalculadorPrecio getCalculadorPrecio() {
        CalculadorPrecio calculador = new PrecioBase(precioBase);
        for (Promocion p : promociones) {
            calculador = p.decorar(calculador);
        }
        return calculador;
    }

    public SkillCoins precioFinal() {
        return getCalculadorPrecio().calcular();
    }

    // ------------------------------------------------------------------ Otros

    public void agregarEtiqueta(String etiqueta) {
        if (etiqueta != null && !etiqueta.isBlank() && !etiquetas.contains(etiqueta.strip())) {
            etiquetas.add(etiqueta.strip());
        }
    }

    public void destacar(boolean destacada) {
        this.destacada = destacada;
    }

    /** Reserva inmediata: las solicitudes quedan ACEPTADAS sin intervención del autor. */
    public void configurarAceptacionAutomatica(boolean automatica) {
        this.aceptacionAutomatica = automatica;
    }

    public boolean isAceptacionAutomatica() {
        return aceptacionAutomatica;
    }

    public boolean esDeAutor(String userId) {
        return autorId.equals(userId);
    }

    public String getId() {
        return id;
    }

    public String getAutorId() {
        return autorId;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public int getDuracionMin() {
        return duracionMin;
    }

    public int getCupo() {
        return cupo;
    }

    public int getCuposOcupados() {
        return cuposOcupados;
    }

    public SkillCoins getPrecioBase() {
        return precioBase;
    }

    public boolean isDestacada() {
        return destacada;
    }

    public List<String> getEtiquetas() {
        return Collections.unmodifiableList(etiquetas);
    }

    public List<Promocion> getPromociones() {
        return Collections.unmodifiableList(promociones);
    }

    public List<FranjaDisponibilidad> getFranjas() {
        return Collections.unmodifiableList(franjas);
    }

    public EstadoPublicacion getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public long getVersion() {
        return version;
    }
}
