-- ============================================================================
-- Papelería POS - Migración V2: Catálogo inicial
-- ============================================================================

INSERT OR IGNORE INTO categories (id, name, description) VALUES
    (1, 'Escolar', 'Artículos escolares y de dibujo'),
    (2, 'Oficina', 'Artículos y consumibles de oficina'),
    (3, 'Papelería y Hojas', 'Papeles, cartulinas y cartoncillos'),
    (4, 'Escritura', 'Lápices, plumas, plumones y marcadores'),
    (5, 'Adhesivos y Pegamentos', 'Pegamentos, cintas y silicón');

INSERT OR IGNORE INTO products (id, name, description, barcode, sku, category_id, purchase_price_cents, sale_price_cents, stock, min_stock, unit, active) VALUES
    (1, 'Cuaderno Profesional 100 Hojas Raya', 'Cuaderno espiral pasta semidura', '750100000001', 'CUA-RAY-01', 1, 1500, 2500, 50, 10, 'pieza', 1),
    (2, 'Cuaderno Profesional 100 Hojas Cuadro Grande', 'Cuaderno espiral cuadro 7mm', '750100000002', 'CUA-CDG-02', 1, 1500, 2500, 45, 10, 'pieza', 1),
    (3, 'Bolígrafo Punto Medio Tinta Negra', 'Bolígrafo retráctil 1.0mm', '750100000003', 'BOL-NEG-01', 4, 350, 700, 100, 20, 'pieza', 1),
    (4, 'Bolígrafo Punto Medio Tinta Azul', 'Bolígrafo retráctil 1.0mm', '750100000004', 'BOL-AZU-02', 4, 350, 700, 100, 20, 'pieza', 1),
    (5, 'Lápiz de Grafito No. 2 HB', 'Lápiz con goma clásico', '750100000005', 'LAP-HB2-01', 4, 200, 500, 80, 15, 'pieza', 1),
    (6, 'Goma de Borrar Blanca', 'Goma de migajón libre de polvo', '750100000006', 'GOM-BLA-01', 1, 300, 600, 60, 10, 'pieza', 1),
    (7, 'Tijeras Escolares Punta Roma', 'Tijeras 13cm acero inoxidable', '750100000007', 'TIJ-ESC-01', 1, 800, 1600, 30, 5, 'pieza', 1),
    (8, 'Pegamento en Barra 21g', 'Pegamento no tóxico lavable', '750100000008', 'PEG-BAR-01', 5, 900, 1800, 40, 10, 'pieza', 1),
    (9, 'Regla de Plástico 30cm', 'Regla transparente graduada', '750100000009', 'REG-PLA-01', 1, 400, 850, 50, 10, 'pieza', 1),
    (10, 'Caja de Colores Largos 12 pzas', 'Lápices de colores de madera', '750100000010', 'COL-12P-01', 1, 2800, 4800, 25, 5, 'caja', 1),
    (11, 'Resma Papel Bond Carta 500 Hojas', 'Papel bond 75g blancura 95%', '750100000011', 'RES-CAR-01', 3, 6500, 9500, 20, 5, 'paquete', 1),
    (12, 'Cartulina Blanca 50x65cm', 'Cartulina bristol blanca', '750100000012', 'CAR-BLA-01', 3, 250, 500, 150, 30, 'pliego', 1);

INSERT INTO schema_version (version, description) VALUES (2, 'Catálogo inicial de papelería');

