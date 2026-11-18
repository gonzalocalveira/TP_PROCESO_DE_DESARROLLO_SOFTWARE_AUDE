package ar.edu.uade.skillswap.dominio.perfil;

import ar.edu.uade.skillswap.dominio.comun.ReglaNegocioException;
import ar.edu.uade.skillswap.dominio.comun.Validar;
import ar.edu.uade.skillswap.dominio.contratacion.Calificacion;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Perfil público de un usuario. Su identidad (userId) la provee el ServicioIdentidad externo.
 * La baja es LÓGICA: se marca inactivo, no se borra (regla del enunciado).
 */
@Entity
@Table(name = "perfiles")
public class Perfil {

    @Id
    @Column(length = 64)
    private String id;
    @Column(name = "nombre_visible", nullable = false, length = 120)
    private String nombreVisible;
    @Column(length = 120)
    private String rol;
    @Column(length = 1000)
    private String bio;
    @Column(name = "zona_horaria", length = 60)
    private String zonaHoraria;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_habilidades", joinColumns = @JoinColumn(name = "perfil_id"))
    @OrderColumn(name = "orden")
    private List<Habilidad> habilidades = new ArrayList<>();
    @Embedded
    @AttributeOverride(name = "promedio", column = @Column(name = "reputacion_promedio", nullable = false))
    @AttributeOverride(name = "cantidad", column = @Column(name = "reputacion_cantidad", nullable = false))
    private Reputacion reputacion = Reputacion.inicial();
    @Column(name = "proyectos_completados", nullable = false)
    private int proyectosCompletados;
    @Column(nullable = false)
    private boolean activo = true;
    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;
    @Version
    private long version;

    protected Perfil() {
        // requerido por JPA
    }

    public Perfil(String userId, String nombreVisible, String rol, String bio, String zonaHoraria) {
        this.id = Validar.texto(userId, "userId", 64);
        actualizarDatos(nombreVisible, rol, bio, zonaHoraria);
    }

    public void actualizarDatos(String nombre, String rol, String bio, String zonaHoraria) {
        exigirActivo();
        this.nombreVisible = Validar.texto(nombre, "nombre", 120);
        this.rol = rol == null ? "" : rol.strip();
        this.bio = bio == null ? "" : bio.strip();
        this.zonaHoraria = zonaHoraria == null ? "America/Argentina/Buenos_Aires" : zonaHoraria;
    }

    public void agregarHabilidad(Habilidad h) {
        exigirActivo();
        if (!habilidades.contains(h)) {
            habilidades.add(h);
        }
    }

    /** Recalcula la reputación a partir de las calificaciones VERIFICADAS recibidas. */
    public void recalcularReputacion(List<Calificacion> calificaciones) {
        Reputacion r = Reputacion.inicial();
        for (Calificacion c : calificaciones) {
            if (c.esVerificada() && c.getCalificadoId().equals(id)) {
                r = r.conNueva(c.getPuntaje());
            }
        }
        this.reputacion = r;
    }

    /** Suma una calificación verificada sin recorrer todo el historial. */
    public void registrarCalificacion(Calificacion c) {
        if (c.esVerificada() && c.getCalificadoId().equals(id)) {
            this.reputacion = reputacion.conNueva(c.getPuntaje());
        }
    }

    public void registrarProyectoCompletado() {
        proyectosCompletados++;
    }

    /** Baja lógica: el perfil deja de operar pero su historial se conserva. */
    public void darDeBaja(LocalDateTime fecha) {
        exigirActivo();
        this.activo = false;
        this.fechaBaja = fecha;
    }

    public boolean esAutorDe(Publicacion p) {
        return id.equals(p.getAutorId());
    }

    public void exigirActivo() {
        if (!activo && id != null) {
            throw new ReglaNegocioException("El perfil " + id + " fue dado de baja.");
        }
    }

    /** Solo para cargar datos históricos de demo (reseñas y proyectos previos a la plataforma). */
    public void importarHistorial(Reputacion reputacion, int proyectosCompletados) {
        this.reputacion = reputacion;
        this.proyectosCompletados = proyectosCompletados;
    }

    public String getId() {
        return id;
    }

    public String getNombreVisible() {
        return nombreVisible;
    }

    public String getRol() {
        return rol;
    }

    public String getBio() {
        return bio;
    }

    public String getZonaHoraria() {
        return zonaHoraria;
    }

    public List<Habilidad> getHabilidades() {
        return Collections.unmodifiableList(habilidades);
    }

    public Reputacion getReputacion() {
        return reputacion;
    }

    public int getProyectosCompletados() {
        return proyectosCompletados;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }
}
