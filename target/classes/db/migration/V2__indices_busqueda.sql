-- =====================================================================
-- SkillSwap - V2: índices para las consultas frecuentes
-- (búsqueda, listados por usuario y bandeja de notificaciones)
-- =====================================================================

CREATE INDEX ix_publicaciones_estado_categoria ON publicaciones (estado, categoria);
CREATE INDEX ix_publicaciones_autor ON publicaciones (autor_id);
CREATE INDEX ix_contrataciones_cliente ON contrataciones (cliente_id);
CREATE INDEX ix_contrataciones_proveedor ON contrataciones (proveedor_id);
CREATE INDEX ix_notificaciones_destinatario ON notificaciones (destinatario_id, fecha);
CREATE INDEX ix_calificaciones_calificado ON calificaciones (calificado_id);
