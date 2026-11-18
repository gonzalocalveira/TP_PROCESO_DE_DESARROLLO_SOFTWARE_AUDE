# SkillSwap — TPO Proceso de Desarrollo de Software

Backend de SkillSwap con **Clean Architecture**, patrones **State, Strategy, Decorator, Observer y Adapter**,
persistencia con **JPA/Hibernate + MySQL**, migraciones **Flyway**, conectado a la interfaz Swing del kit
(`SkillSwapView` y `UiControllers` no se modificaron).

## Cómo ejecutar

```bash
mvn clean test          # corre los 60 tests (dominio, casos de aceptación, concurrencia, adapters, controllers)
mvn exec:java           # abre la app con los controllers reales
mvn exec:java -Dexec.args="--mock"   # abre la app con el mock original del kit
```

Desde Eclipse: *Run As > Java Application* sobre `SkillSwapApplication`.

### Base de datos

Por defecto usa **MySQL** (`src/main/resources/skillswap.properties`):

```
db.mysql.url=jdbc:mysql://localhost:3306/skillswap?createDatabaseIfNotExist=true&...
db.mysql.usuario=root
db.mysql.password=root
```

Al arrancar, **Flyway** crea/actualiza el esquema (`db/migration/V1__esquema_inicial.sql`, `V2__indices_busqueda.sql`)
y, si la base está vacía, se cargan **datos de demo** (usuarios, publicaciones, contrataciones con historial,
movimientos y notificaciones).

Si MySQL no está disponible, la app arranca igual con **H2 en memoria** (`db.fallbackH2=true`) y lo indica en el
título de la ventana. Se puede forzar con `-Ddb.modo=h2`. Cualquier clave se puede pisar con `-Dclave=valor`
o con un `skillswap.properties` en la carpeta desde donde se ejecuta.

## Estructura (Clean Architecture)

```
ar.edu.uade.skillswap
├── dominio            ← reglas de negocio (sin Swing; entidades mapeadas con anotaciones JPA)
│   ├── publicacion    Publicacion (+ OfertaMentoria, OfertaServicio, Desafio), FranjaDisponibilidad, Promocion
│   │   └── estado     STATE: Borrador, Publicada, Pausada, Cerrada, Cancelada
│   ├── contratacion   Contratacion, HistorialEstado, Calificacion
│   │   └── estado     STATE: Pendiente, Aceptada, Confirmada, EnCurso, Completada, Rechazada, Cancelada
│   ├── precio         DECORATOR: CalculadorPrecio, PrecioBase, PrecioDecorator, PromocionDecorator, ComisionDecorator
│   ├── recomendacion  STRATEGY: RecomendacionStrategy, AfinidadPorHabilidades, PopularidadReciente
│   ├── cancelacion    STRATEGY: PoliticaCancelacion, ReintegroTotal, ReintegroParcial, SinReintegro, ReglasCancelacion
│   ├── evento         OBSERVER: PublicadorEventos (sujeto), EventoObserver, EventoDominio
│   ├── perfil, wallet, notificacion, comun
├── aplicacion         ← casos de uso (GestionContratacion, GestionPublicacion, ...)
│   ├── puertos        interfaces: repositorios, UnidadDeTrabajo, Notificador, ServicioIdentidad, ProcesadorPagos
│   └── NotificacionObserver, AuditoriaObserver (OBSERVER concretos)
├── infraestructura    ← detalles reemplazables
│   ├── persistencia   repositorios JPA, JpaUnidadDeTrabajo, converters, Flyway
│   ├── identidad      ADAPTER: IdentidadMockAdapter → ServicioIdentidadSDK
│   ├── notificaciones ADAPTER: EmailAdapter, PushAdapter, NotificacionInternaAdapter
│   ├── pagos          ADAPTER: PasarelaSkillCoinsAdapter → SkillCoinsGatewaySDK
│   └── config         SkillSwapContexto (composition root), DatosDemo, Configuracion
├── controllers        UiControllers (kit) + SkillSwapControllers (implementación real)
├── model, ui, mock    ← kit del profesor, sin cambios
```

Las dependencias apuntan hacia adentro: la aplicación define puertos y la infraestructura los implementa.
`SkillSwapContexto` arma las clases concretas. Las entidades declaran su mapeo con anotaciones Jakarta;
los adaptadores JPA implementan los puertos y están marcados con `@Repository` de Spring.

Cada uso de patrón está marcado en el código con un comentario `PATRÓN <NOMBRE> — <rol>`.

## Reglas del enunciado y dónde se validan

| Regla | Dónde |
|---|---|
| El autor no solicita su propia publicación | `Contratacion.validarSolicitud` |
| Solo publicaciones PUBLICADAS reciben solicitudes | estado `Publicada.aceptaSolicitudes()` |
| No reservar franja ocupada ni superar cupo | `Publicacion.reservar` + `@Version` (bloqueo optimista) |
| Débito y crédito en una sola transacción | `GestionContratacion.pagar` dentro de `UnidadDeTrabajo` |
| Reintegro 100/50/0 según anticipación configurable | `ReglasCancelacion` + estrategias `PoliticaCancelacion` |
| Promociones acumulables, total nunca negativo | `PrecioDecorator.calcular()` |
| Calificación única, solo participantes de COMPLETADA, no a sí mismo | `Calificacion.crear` + UNIQUE en la base |
| Historial inmutable (fecha, actor, motivo) | `HistorialEstado` (sin setters, lista no modificable) |
| Baja lógica de perfiles | `Perfil.darDeBaja` |
| Controllers validan permisos con el userId | `GestionPerfil.validarSesion` + reglas de rol en `Contratacion` |
| Paginación | `FiltroBusqueda` / `Pagina` / `JpaPublicacionRepository.buscar` |

## Usuarios de demo

La UI del kit usa siempre `user-mateo` (Mateo Silva). En los datos de demo Mateo es cliente de dos contrataciones
(una completada y otra confirmada) y proveedor de una solicitud **pendiente** de Lucía: en *Contrataciones*,
"Avanzar estado" la acepta y "Cancelar" la rechaza. Las publicaciones de otros proveedores aceptan
automáticamente, así que al solicitar una se puede seguir el flujo completo: pagar → iniciar → completar.
