package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;
import com.papeleria.pos.dao.InventoryMovementDao;
import com.papeleria.pos.dao.ProductDao;
import com.papeleria.pos.model.InventoryMovement;
import com.papeleria.pos.model.Product;
import com.papeleria.pos.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Service class handling business logic for Products.
 */
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductDao productDao;
    private final InventoryMovementDao inventoryMovementDao;

    public ProductService() {
        this.productDao = new ProductDao();
        this.inventoryMovementDao = new InventoryMovementDao();
    }

    public List<Product> getAll() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return productDao.getAll(conn);
        } catch (SQLException e) {
            logger.error("Error retrieving all products", e);
            throw new RuntimeException("Database error retrieving products", e);
        }
    }

    public Product getById(int id) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return productDao.getById(conn, id);
        } catch (SQLException e) {
            logger.error("Error retrieving product with id: {}", id, e);
            throw new RuntimeException("Database error retrieving product", e);
        }
    }

    public Product getByBarcode(String barcode) {
        if (barcode == null || barcode.trim().isEmpty()) return null;
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return productDao.getByBarcode(conn, barcode.trim());
        } catch (SQLException e) {
            logger.error("Error retrieving product with barcode: {}", barcode, e);
            throw new RuntimeException("Database error retrieving product", e);
        }
    }

    public void createProduct(Product product) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            if (product.getBarcode() != null) {
                String trimmed = product.getBarcode().trim();
                product.setBarcode(trimmed.isEmpty() ? null : trimmed);
            }
            if (product.getBarcode() != null) {
                Product existing = productDao.getByBarcode(conn, product.getBarcode());
                if (existing != null) {
                    throw new RuntimeException("Ya existe un producto con el código de barras: " + product.getBarcode());
                }
            }
            productDao.insert(conn, product);
            
            // Record initial stock movement if stock > 0
            if (product.getStock() > 0) {
                try {
                    InventoryMovement movement = new InventoryMovement();
                    movement.setProductId(product.getId());
                    movement.setType("INITIAL");
                    movement.setQuantity(product.getStock());
                    movement.setPreviousStock(0);
                    movement.setNewStock(product.getStock());
                    movement.setReason("Inventario inicial al registrar producto");
                    movement.setCreatedAt(DateUtil.now());
                    inventoryMovementDao.insert(conn, movement);
                } catch (Exception ex) {
                    logger.warn("Could not log initial movement for product {}: {}", product.getId(), ex.getMessage());
                }
            }
            
            logger.info("Created new product with barcode: {}", product.getBarcode());
        } catch (SQLException e) {
            logger.error("Error creating product: {}", product.getBarcode(), e);
            throw new RuntimeException("Error en base de datos al crear producto: " + e.getMessage(), e);
        }
    }

    public void updateProduct(Product product) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            if (product.getBarcode() != null) {
                String trimmed = product.getBarcode().trim();
                product.setBarcode(trimmed.isEmpty() ? null : trimmed);
            }
            if (product.getBarcode() != null) {
                Product existing = productDao.getByBarcode(conn, product.getBarcode());
                if (existing != null && existing.getId() != product.getId()) {
                    throw new RuntimeException("Ya existe otro producto con el código de barras: " + product.getBarcode());
                }
            }
            
            // Track stock change if edited directly
            Product old = productDao.getById(conn, product.getId());
            if (old != null && old.getStock() != product.getStock()) {
                int diff = product.getStock() - old.getStock();
                try {
                    InventoryMovement movement = new InventoryMovement();
                    movement.setProductId(product.getId());
                    movement.setType(diff > 0 ? "ADJUSTMENT" : "LOSS");
                    movement.setQuantity(diff);
                    movement.setPreviousStock(old.getStock());
                    movement.setNewStock(product.getStock());
                    movement.setReason("Ajuste directo desde edición de producto");
                    movement.setCreatedAt(DateUtil.now());
                    inventoryMovementDao.insert(conn, movement);
                } catch (Exception ex) {
                    logger.warn("Could not log stock adjustment movement for product {}: {}", product.getId(), ex.getMessage());
                }
            }
            
            productDao.update(conn, product);
            logger.info("Updated product ID: {}", product.getId());
        } catch (SQLException e) {
            logger.error("Error updating product ID: {}", product.getId(), e);
            throw new RuntimeException("Error en base de datos al actualizar producto: " + e.getMessage(), e);
        }
    }

    public boolean deleteProduct(int productId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            Product product = productDao.getById(conn, productId);
            if (product == null) {
                throw new RuntimeException("El producto no existe");
            }
            boolean hasHist = productDao.hasHistory(conn, productId);
            if (!hasHist) {
                try {
                    productDao.delete(conn, productId);
                    logger.info("Permanently deleted product ID: {}", productId);
                    return true;
                } catch (Exception e) {
                    logger.warn("Could not permanently delete product ID: {}, deactivating instead", productId);
                    productDao.deactivate(conn, productId);
                    return false;
                }
            } else {
                productDao.deactivate(conn, productId);
                logger.info("Product ID: {} has history; deactivated safely", productId);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error deleting product ID: {}", productId, e);
            throw new RuntimeException("Error en base de datos al eliminar producto: " + e.getMessage(), e);
        }
    }

    public void deactivateProduct(int productId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            productDao.deactivate(conn, productId);
            logger.info("Deactivated product ID: {}", productId);
        } catch (SQLException e) {
            logger.error("Error deactivating product ID: {}", productId, e);
            throw new RuntimeException("Database error deactivating product", e);
        }
    }

    public List<Product> search(String term) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return productDao.search(conn, term);
        } catch (SQLException e) {
            logger.error("Error searching products with term: {}", term, e);
            throw new RuntimeException("Database error searching products", e);
        }
    }

    public List<Product> getLowStockAlerts() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return productDao.getLowStock(conn);
        } catch (SQLException e) {
            logger.error("Error retrieving low stock alerts", e);
            throw new RuntimeException("Database error retrieving low stock alerts", e);
        }
    }
}
