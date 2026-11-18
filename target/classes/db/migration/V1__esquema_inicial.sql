-- =====================================================================
-- SkillSwap - V1: esquema inicial (Flyway)
-- Compatible con MySQL 8+ (y con H2 en modo MySQL, usado en los tests).
-- =====================================================================

CREATE TABLE perfiles (
    id                     VARCHAR(64)   NOT NULL PRIMARY KEY,
    nombre_visible         VARCHAR(120)  NOT NULL,
    rol                    VARCHAR(120),
    bio                    VARCHAR(1000),
    zona_horaria           VARCHAR(60),
    reputacion_promedio    DOUBLE        NOT NULL DEFAULT 0,
    reputacion_cantidad    INT           NOT NULL DEFAULT 0,
    proyectos_completados  INT           NOT NULL DEFAULT 0,
    activo                 BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_baja             DATETIME(6),
    version                BIGINT        NOT NULL DEFAULT 0
);

CREATE TABLE perfil_habilidades (
    perfil_id  VARCHAR(64)  NOT NULL,
    orden      INT          NOT NULL,
    nombre     VARCHAR(80),
    categoria  VARCHAR(80),
    PRIMARY KEY (perfil_id, orden),
    CONSTRAINT fk_habilidad_perfil FOREIGN KEY (perfil_id) REFERENCES perfiles (id) ON DELETE CASCADE
);

CREATE TABLE publicaciones (
    id                     VARCHAR(64)    NOT NULL PRIMARY KEY,
    tipo                   VARCHAR(20)    NOT NULL,
    autor_id               VARCHAR(64)    NOT NULL,
    titulo                 VARCHAR(150)   NOT NULL,
    descripcion            VARCHAR(2000),
    categoria              VARCHAR(80)    NOT NULL,
    modalidad              VARCHAR(20)    NOT NULL,
    duracion_min           INT            NOT NULL,
    cupo                   INT            NOT NULL,
    cupos_ocupados         INT            NOT NULL DEFAULT 0,
    precio_base            DECIMAL(12, 0) NOT NULL,
    destacada              BOOLEAN        NOT NULL DEFAULT FALSE,
    aceptacion_automatica  BOOLEAN        NOT NULL DEFAULT FALSE,
    estado                 VARCHAR(20)    NOT NULL,
    fecha_creacion         DATETIME(6)    NOT NULL,
    fecha_publicacion      DATETIME(6),
    version                BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_publicacion_autor FOREIGN KEY (autor_id) REFERENCES perfiles (id),
    CONSTRAINT ck_publicacion_cupo CHECK (cupos_ocupados >= 0 AND cupos_ocupados <= cupo)
);

CREATE TABLE publicacion_etiquetas (
    publicacion_id  VARCHAR(64)  NOT NULL,
    orden           INT          NOT NULL,
    etiqueta        VARCHAR(60),
    PRIMARY KEY (publicacion_id, orden),
    CONSTRAINT fk_etiqueta_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id) ON DELETE CASCADE
);

CREATE TABLE publicacion_promociones (
    publicacion_id  VARCHAR(64)    NOT NULL,
    orden           INT            NOT NULL,
    descripcion     VARCHAR(120),
    porcentaje      DOUBLE         NOT NULL DEFAULT 0,
    monto_fijo      DECIMAL(12, 0) NOT NULL DEFAULT 0,
    PRIMARY KEY (publicacion_id, orden),
    CONSTRAINT fk_promocion_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id) ON DELETE CASCADE
);

CREATE TABLE franjas_disponibilidad (
    id              VARCHAR(64)  NOT NULL PRIMARY KEY,
    publicacion_id  VARCHAR(64)  NOT NULL,
    orden           INT,
    inicio          DATETIME(6)  NOT NULL,
    fin             DATETIME(6)  NOT NULL,
    ocupada         BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_franja_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id) ON DELETE CASCADE
);

CREATE TABLE contrataciones (
    id              VARCHAR(64)    NOT NULL PRIMARY KEY,
    publicacion_id  VARCHAR(64)    NOT NULL,
    cliente_id      VARCHAR(64)    NOT NULL,
    proveedor_id    VARCHAR(64)    NOT NULL,
    titulo          VARCHAR(150)   NOT NULL,
    mensaje         VARCHAR(1000),
    objetivo        VARCHAR(500),
    total           DECIMAL(12, 0) NOT NULL,
    comision        DECIMAL(12, 0) NOT NULL DEFAULT 0,
    reintegro       DECIMAL(12, 0) NOT NULL DEFAULT 0,
    franja_id       VARCHAR(64)    NOT NULL,
    fecha_sesion    DATETIME(6)    NOT NULL,
    fin_sesion      DATETIME(6)    NOT NULL,
    estado          VARCHAR(20)    NOT NULL,
    fecha_creacion  DATETIME(6)    NOT NULL,
    version         BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT fk_contratacion_publicacion FOREIGN KEY (publicacion_id) REFERENCES publicaciones (id),
    CONSTRAINT fk_contratacion_cliente FOREIGN KEY (cliente_id) REFERENCES perfiles (id),
    CONSTRAINT fk_contratacion_proveedor FOREIGN KEY (proveedor_id) REFERENCES perfiles (id),
    CONSTRAINT ck_contratacion_autocontratacion CHECK (cliente_id <> proveedor_id)
);

-- Historial inmutable: la aplicación solo inserta filas (nunca UPDATE ni DELETE).
CREATE TABLE contratacion_historial (
    contratacion_id  VARCHAR(64)   NOT NULL,
    orden            INT           NOT NULL,
    fecha            DATETIME(6)   NOT NULL,
    actor            VARCHAR(64)   NOT NULL,
    motivo           VARCHAR(300),
    estado_anterior  VARCHAR(20),
    estado_nuevo     VARCHAR(20)   NOT NULL,
    PRIMARY KEY (contratacion_id, orden),
    CONSTRAINT fk_historial_contratacion FOREIGN KEY (contratacion_id) REFERENCES contrataciones (id)
);

CREATE TABLE calificaciones (
    id               VARCHAR(64)   NOT NULL PRIMARY KEY,
    contratacion_id  VARCHAR(64)   NOT NULL,
    autor_id         VARCHAR(64)   NOT NULL,
    calificado_id    VARCHAR(64)   NOT NULL,
    puntaje          INT           NOT NULL,
    comentario       VARCHAR(500),
    verificada       BOOLEAN       NOT NULL DEFAULT FALSE,
    fecha            DATETIME(6)   NOT NULL,
    CONSTRAINT uk_calificacion_unica UNIQUE (contratacion_id, autor_id),
    CONSTRAINT fk_calificacion_contratacion FOREIGN KEY (contratacion_id) REFERENCES contrataciones (id),
    CONSTRAINT ck_calificacion_puntaje CHECK (puntaje BETWEEN 1 AND 5),
    CONSTRAINT ck_calificacion_no_propia CHECK (autor_id <> calificado_id)
);

CREATE TABLE wallets (
    perfil_id  VARCHAR(64)    NOT NULL PRIMARY KEY,
    saldo      DECIMAL(14, 0) NOT NULL DEFAULT 0,
    version    BIGINT         NOT NULL DEFAULT 0,
    CONSTRAINT ck_wallet_saldo CHECK (saldo >= 0)
);

CREATE TABLE wallet_movimientos (
    wallet_id         VARCHAR(64)    NOT NULL,
    orden             INT            NOT NULL,
    concepto          VARCHAR(200)   NOT NULL,
    monto             DECIMAL(14, 0) NOT NULL,
    debito            BOOLEAN        NOT NULL,
    fecha             DATETIME(6)    NOT NULL,
    saldo_resultante  DECIMAL(14, 0) NOT NULL,
    comprobante       VARCHAR(80),
    PRIMARY KEY (wallet_id, orden),
    CONSTRAINT fk_movimiento_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (perfil_id)
);

CREATE TABLE notificaciones (
    id               VARCHAR(64)   NOT NULL PRIMARY KEY,
    destinatario_id  VARCHAR(64)   NOT NULL,
    tipo             VARCHAR(30)   NOT NULL,
    titulo           VARCHAR(150)  NOT NULL,
    mensaje          VARCHAR(500),
    fecha            DATETIME(6)   NOT NULL,
    leida            BOOLEAN       NOT NULL DEFAULT FALSE
);
