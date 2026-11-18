package ar.edu.uade.skillswap.infraestructura.persistencia.jpa;

import ar.edu.uade.skillswap.aplicacion.puertos.FiltroBusqueda;
import ar.edu.uade.skillswap.aplicacion.puertos.Pagina;
import ar.edu.uade.skillswap.aplicacion.puertos.PublicacionRepository;
import ar.edu.uade.skillswap.dominio.comun.SkillCoins;
import ar.edu.uade.skillswap.dominio.publicacion.Publicacion;
import ar.edu.uade.skillswap.dominio.publicacion.estado.Publicada;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación JPA del puerto PublicacionRepository.
 * La búsqueda arma JPQL dinámico, pagina en la base (setFirstResult/setMaxResults)
 * y solo devuelve publicaciones PUBLICADAS de autores activos (baja lógica).
 */
@Repository
public class JpaPublicacionRepository extends RepositorioJpa<Publicacion> implements PublicacionRepository {

    private static final String BASE =
            " from Publicacion p, Perfil a where a.id = p.autorId and a.activo = true and p.estado = :estado";

    public JpaPublicacionRepository() {
        super(Publicacion.class);
    }

    @Override
    public void guardar(Publicacion publicacion) {
        guardarEntidad(publicacion, publicacion.getId());
    }

    @Override
    public Optional<Publicacion> porId(String id) {
        return Optional.ofNullable(buscar(id));
    }

    @Override
    public Pagina<Publicacion> buscar(FiltroBusqueda f) {
        StringBuilder where = new StringBuilder(BASE);
        Map<String, Object> params = new HashMap<>();
        params.put("estado", new Publicada());
        if (f.texto() != null) {
            where.append(" and (lower(p.titulo) like :t or lower(p.descripcion) like :t or lower(p.categoria) like :t")
                    .append(" or lower(a.nombreVisible) like :t")
                    .append(" or exists (select 1 from Publicacion p2 join p2.etiquetas e where p2 = p and lower(e) like :t))");
            params.put("t", "%" + f.texto().toLowerCase(Locale.ROOT) + "%");
        }
        if (f.categoria() != null) {
            where.append(" and p.categoria = :cat");
            params.put("cat", f.categoria());
        }
        if (f.modalidad() != null) {
            where.append(" and p.modalidad = :mod");
            params.put("mod", f.modalidad());
        }
        if (f.precioMaximo() != null) {
            where.append(" and p.precioBase <= :pmax");
            params.put("pmax", SkillCoins.de(f.precioMaximo()));
        }
        String orden = switch (f.orden()) {
            case MEJOR_REPUTACION -> " order by a.reputacion.promedio desc, a.reputacion.cantidad desc, p.id";
            case MENOR_PRECIO -> " order by p.precioBase asc, p.id";
            case RELEVANCIA -> " order by p.destacada desc, a.reputacion.promedio desc, p.fechaPublicacion desc, p.id";
        };

        TypedQuery<Long> count = em().createQuery("select count(p)" + where, Long.class);
        TypedQuery<Publicacion> query = em().createQuery("select p" + where + orden, Publicacion.class);
        params.forEach((k, v) -> {
            count.setParameter(k, v);
            query.setParameter(k, v);
        });
        long total = count.getSingleResult();
        List<Publicacion> contenido = query
                .setFirstResult(f.pagina() * f.tamanio())
                .setMaxResults(f.tamanio())
                .getResultList();
        return new Pagina<>(contenido, f.pagina(), f.tamanio(), total);
    }

    @Override
    public List<Publicacion> porAutor(String autorId) {
        return em().createQuery("select p from Publicacion p where p.autorId = :autor", Publicacion.class)
                .setParameter("autor", autorId)
                .getResultList();
    }

    @Override
    public List<Publicacion> publicadas() {
        return em().createQuery("select p" + BASE, Publicacion.class)
                .setParameter("estado", new Publicada())
                .getResultList();
    }
}
