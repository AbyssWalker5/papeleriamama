-- ============================================================================
-- Papelería POS - Migración V3: Sincronizar contador de autoincremento
-- ============================================================================

UPDATE sqlite_sequence SET seq = (SELECT COALESCE(MAX(id), 0) FROM products) WHERE name = 'products';

INSERT INTO schema_version (version, description) VALUES (3, 'Sincronizar secuencia de autoincremento');

