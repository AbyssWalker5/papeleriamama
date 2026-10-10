package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;

import com.papeleria.pos.dao.InventoryMovementDao;
import com.papeleria.pos.dao.ProductDao;
import com.papeleria.pos.model.InventoryMovement;
import com.papeleria.pos.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Service class handling business logic for Inventory.
 */
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    
    private final ProductDao productDao = new ProductDao();
    private final InventoryMovementDao inventoryMovementDao = new InventoryMovementDao();
    
    private static final List<String> VALID_REMOVAL_TYPES = Arrays.asList("DAMAGE", "LOSS", "ADJUSTMENT");

    public void updateStock(int productId, int quantity, String reason) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        executeInventoryChange(productId, quantity, reason, "PURCHASE", true);
    }

    public void removeStock(int productId, int quantity, String reason, String type) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (type == null || !VALID_REMOVAL_TYPES.contains(type.toUpperCase())) {
            throw new IllegalArgumentException("Invalid removal type. Must be one of: " + VALID_REMOVAL_TYPES);
        }
        executeInventoryChange(productId, quantity, reason, type.toUpperCase(), false);
    }

    private void executeInventoryChange(int productId, int quantity, String reason, String type, boolean isAdd) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getTransactionConnection();

            Product product = productDao.getById(conn, productId);
            if (product == null) {
                throw new IllegalArgumentException("Product not found with ID: " + productId);
            }

            if (!isAdd && product.getStock() < quantity) {
                throw new IllegalStateException("Insufficient stock to remove " + quantity + " units from product ID: " + productId);
            }

            // Update product stock
            if (isAdd) {
                productDao.updateStock(conn, productId, quantity);
            } else {
                productDao.updateStock(conn, productId, -quantity);
            }

            // Record movement
            InventoryMovement movement = new InventoryMovement();
            movement.setProductId(productId);
            movement.setQuantity(quantity);
            movement.setType(type);
            movement.setCreatedAt(LocalDateTime.now());
            movement.setReason(reason);
            
            inventoryMovementDao.insert(conn, movement);

            conn.commit();
            logger.info("Successfully recorded inventory {} of {} for product ID: {}", type, quantity, productId);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction for inventory update", ex);
                }
            }
            logger.error("Failed to update inventory for product ID: {}", productId, e);
            throw new RuntimeException("Failed to update inventory: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }
}

