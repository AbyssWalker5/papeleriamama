-- ============================================================================
-- Papelería POS - Migración V1: Esquema inicial
-- ============================================================================
-- Convenciones:
--   - Montos monetarios almacenados como INTEGER en centavos
--   - Fechas almacenadas como TEXT en formato ISO 8601 (UTC)
--   - FK constraints habilitadas en cada conexión (PRAGMA foreign_keys = ON)
-- ============================================================================

-- Tabla de control de migraciones
CREATE TABLE IF NOT EXISTS schema_version (
    version     INTEGER PRIMARY KEY,
    description TEXT    NOT NULL,
    applied_at  TEXT    NOT NULL DEFAULT (datetime('now'))
);

-- ============================================================================
-- CATEGORÍAS
-- ============================================================================
CREATE TABLE categories (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT    NOT NULL UNIQUE,
    description TEXT,
    active      INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
);

-- ============================================================================
-- PRODUCTOS
-- ============================================================================
CREATE TABLE products (
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    name                 TEXT    NOT NULL,
    description          TEXT,
    barcode              TEXT    UNIQUE,
    sku                  TEXT,
    category_id          INTEGER,
    purchase_price_cents INTEGER NOT NULL CHECK (purchase_price_cents >= 0),
    sale_price_cents     INTEGER NOT NULL CHECK (sale_price_cents > 0),
    stock                INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    min_stock            INTEGER NOT NULL DEFAULT 0 CHECK (min_stock >= 0),
    unit                 TEXT    NOT NULL DEFAULT 'pieza',
    active               INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at           TEXT    NOT NULL DEFAULT (datetime('now')),
    updated_at           TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_products_barcode     ON products(barcode);
CREATE INDEX idx_products_name        ON products(name);
CREATE INDEX idx_products_category    ON products(category_id);
CREATE INDEX idx_products_active      ON products(active);
CREATE INDEX idx_products_stock_alert ON products(stock, min_stock) WHERE active = 1;

-- ============================================================================
-- SESIONES DE CAJA
-- ============================================================================
CREATE TABLE cash_sessions (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    initial_fund_cents INTEGER NOT NULL CHECK (initial_fund_cents >= 0),
    status            TEXT    NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'CLOSED')),
    opened_at         TEXT    NOT NULL DEFAULT (datetime('now')),
    closed_at         TEXT
);

CREATE INDEX idx_cash_sessions_status ON cash_sessions(status);

-- ============================================================================
-- VENTAS
-- ============================================================================
CREATE TABLE sales (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_number       TEXT    NOT NULL UNIQUE,
    idempotency_token TEXT    NOT NULL UNIQUE,
    cash_session_id   INTEGER NOT NULL,
    subtotal_cents    INTEGER NOT NULL CHECK (subtotal_cents >= 0),
    discount_cents    INTEGER NOT NULL DEFAULT 0 CHECK (discount_cents >= 0),
    total_cents       INTEGER NOT NULL CHECK (total_cents >= 0),
    payment_method    TEXT    NOT NULL CHECK (payment_method IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'OTRO')),
    amount_paid_cents INTEGER CHECK (amount_paid_cents >= 0),
    change_cents      INTEGER DEFAULT 0 CHECK (change_cents >= 0),
    status            TEXT    NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('COMPLETED', 'CANCELLED')),
    created_at        TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cash_session_id) REFERENCES cash_sessions(id)
);

CREATE INDEX idx_sales_session ON sales(cash_session_id);
CREATE INDEX idx_sales_date    ON sales(created_at);
CREATE INDEX idx_sales_status  ON sales(status);
CREATE INDEX idx_sales_number  ON sales(sale_number);
CREATE INDEX idx_sales_payment ON sales(payment_method);

-- ============================================================================
-- DETALLE DE VENTAS (con snapshots inmutables)
-- ============================================================================
CREATE TABLE sale_items (
    id                       INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id                  INTEGER NOT NULL,
    product_id               INTEGER NOT NULL,
    product_name_snapshot    TEXT    NOT NULL,
    product_barcode_snapshot TEXT,
    unit_price_cents_snapshot INTEGER NOT NULL CHECK (unit_price_cents_snapshot >= 0),
    quantity                 INTEGER NOT NULL CHECK (quantity > 0),
    discount_cents           INTEGER NOT NULL DEFAULT 0 CHECK (discount_cents >= 0),
    subtotal_cents           INTEGER NOT NULL CHECK (subtotal_cents >= 0),
    FOREIGN KEY (sale_id)    REFERENCES sales(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_sale_items_sale    ON sale_items(sale_id);
CREATE INDEX idx_sale_items_product ON sale_items(product_id);

-- ============================================================================
-- MOVIMIENTOS DE CAJA
-- ============================================================================
CREATE TABLE cash_movements (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    cash_session_id   INTEGER NOT NULL,
    type              TEXT    NOT NULL CHECK (type IN ('SALE', 'CASH_IN', 'CASH_OUT', 'REFUND', 'INITIAL')),
    amount_cents      INTEGER NOT NULL CHECK (amount_cents > 0),
    reason            TEXT,
    related_sale_id   INTEGER,
    related_return_id INTEGER,
    created_at        TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cash_session_id)   REFERENCES cash_sessions(id),
    FOREIGN KEY (related_sale_id)   REFERENCES sales(id),
    FOREIGN KEY (related_return_id) REFERENCES returns(id)
);

CREATE INDEX idx_cash_movements_session ON cash_movements(cash_session_id);
CREATE INDEX idx_cash_movements_type    ON cash_movements(type);
CREATE INDEX idx_cash_movements_date    ON cash_movements(created_at);

-- ============================================================================
-- CIERRES DE CAJA
-- ============================================================================
CREATE TABLE cash_closures (
    id                  INTEGER PRIMARY KEY AUTOINCREMENT,
    cash_session_id     INTEGER NOT NULL UNIQUE,
    initial_fund_cents  INTEGER NOT NULL,
    cash_sales_cents    INTEGER NOT NULL DEFAULT 0,
    card_sales_cents    INTEGER NOT NULL DEFAULT 0,
    transfer_sales_cents INTEGER NOT NULL DEFAULT 0,
    other_sales_cents   INTEGER NOT NULL DEFAULT 0,
    total_sales_cents   INTEGER NOT NULL DEFAULT 0,
    total_transactions  INTEGER NOT NULL DEFAULT 0,
    cash_in_cents       INTEGER NOT NULL DEFAULT 0,
    cash_out_cents      INTEGER NOT NULL DEFAULT 0,
    returns_cents       INTEGER NOT NULL DEFAULT 0,
    expected_cash_cents INTEGER NOT NULL,
    counted_cash_cents  INTEGER NOT NULL,
    difference_cents    INTEGER NOT NULL,
    observations        TEXT,
    created_at          TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cash_session_id) REFERENCES cash_sessions(id)
);

-- ============================================================================
-- CORRECCIONES A CIERRES (auditoría)
-- ============================================================================
CREATE TABLE cash_closure_corrections (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    cash_closure_id  INTEGER NOT NULL,
    field_corrected  TEXT    NOT NULL,
    old_value        TEXT    NOT NULL,
    new_value        TEXT    NOT NULL,
    justification    TEXT    NOT NULL,
    created_at       TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (cash_closure_id) REFERENCES cash_closures(id)
);

-- ============================================================================
-- MOVIMIENTOS DE INVENTARIO
-- ============================================================================
CREATE TABLE inventory_movements (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id     INTEGER NOT NULL,
    type           TEXT    NOT NULL CHECK (type IN ('PURCHASE', 'SALE', 'RETURN', 'ADJUSTMENT', 'DAMAGE', 'LOSS', 'INITIAL')),
    quantity       INTEGER NOT NULL,
    previous_stock INTEGER NOT NULL,
    new_stock      INTEGER NOT NULL CHECK (new_stock >= 0),
    reason         TEXT,
    created_at     TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_inv_movements_product ON inventory_movements(product_id);
CREATE INDEX idx_inv_movements_type    ON inventory_movements(type);
CREATE INDEX idx_inv_movements_date    ON inventory_movements(created_at);

-- ============================================================================
-- DEVOLUCIONES
-- ============================================================================
CREATE TABLE returns (
    id                    INTEGER PRIMARY KEY AUTOINCREMENT,
    sale_id               INTEGER NOT NULL,
    return_number         TEXT    NOT NULL UNIQUE,
    total_cents           INTEGER NOT NULL CHECK (total_cents >= 0),
    reason                TEXT    NOT NULL,
    payment_method_refund TEXT    NOT NULL CHECK (payment_method_refund IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA', 'OTRO')),
    status                TEXT    NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('COMPLETED', 'CANCELLED')),
    created_at            TEXT    NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (sale_id) REFERENCES sales(id)
);

CREATE INDEX idx_returns_sale ON returns(sale_id);

-- ============================================================================
-- DETALLE DE DEVOLUCIONES
-- ============================================================================
CREATE TABLE return_items (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    return_id        INTEGER NOT NULL,
    product_id       INTEGER NOT NULL,
    sale_item_id     INTEGER NOT NULL,
    quantity         INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_cents INTEGER NOT NULL CHECK (unit_price_cents >= 0),
    subtotal_cents   INTEGER NOT NULL CHECK (subtotal_cents >= 0),
    FOREIGN KEY (return_id)   REFERENCES returns(id),
    FOREIGN KEY (product_id)  REFERENCES products(id),
    FOREIGN KEY (sale_item_id) REFERENCES sale_items(id)
);

CREATE INDEX idx_return_items_return ON return_items(return_id);

-- ============================================================================
-- CONFIGURACIÓN DE LA APLICACIÓN
-- ============================================================================
CREATE TABLE app_settings (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    key         TEXT    NOT NULL UNIQUE,
    value       TEXT,
    description TEXT
);

-- Valores iniciales de configuración
INSERT INTO app_settings (key, value, description) VALUES
    ('store_name',       'Mi Papelería',           'Nombre de la papelería'),
    ('store_address',    'Dirección de la tienda',  'Dirección para tickets'),
    ('store_phone',      '000-000-0000',            'Teléfono de contacto'),
    ('thank_you_message','¡Gracias por su compra!', 'Mensaje de agradecimiento en tickets'),
    ('ticket_width_mm',  '80',                      'Ancho del papel para tickets (58 o 80)'),
    ('backup_max_count', '10',                      'Número máximo de respaldos a conservar'),
    ('tax_rate',         '0',                       'Tasa de impuesto (porcentaje, 0 = sin impuesto)');

-- Registrar la migración
INSERT INTO schema_version (version, description) VALUES (1, 'Esquema inicial completo');

