package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;

import com.papeleria.pos.dao.CashMovementDao;
import com.papeleria.pos.dao.CashSessionDao;
import com.papeleria.pos.dao.InventoryMovementDao;
import com.papeleria.pos.dao.ProductDao;
import com.papeleria.pos.dao.SaleDao;
import com.papeleria.pos.model.CashMovement;
import com.papeleria.pos.model.InventoryMovement;
import com.papeleria.pos.model.Product;
import com.papeleria.pos.model.Sale;
import com.papeleria.pos.model.SaleItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class handling business logic for Sales.
 */
public class SaleService {
    private static final Logger logger = LoggerFactory.getLogger(SaleService.class);
    
    private final SaleDao saleDao = new SaleDao();
    private final ProductDao productDao = new ProductDao();
    private final InventoryMovementDao inventoryMovementDao = new InventoryMovementDao();
    private final CashMovementDao cashMovementDao = new CashMovementDao();
    private final CashSessionDao cashSessionDao = new CashSessionDao();

    public void completeSale(Sale sale) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getTransactionConnection();

            // Validate session is OPEN
            if (cashSessionDao.getActiveSession(conn) == null) {
                throw new IllegalStateException("Cannot complete sale: Cash session is not OPEN");
            }

            // Validate idempotencyToken
            if (saleDao.existsByIdempotencyToken(conn, sale.getIdempotencyToken())) {
                throw new IllegalStateException("Sale with this idempotency token already exists");
            }

            // Validate stock for all items before reducing
            for (SaleItem item : sale.getItems()) {
                Product product = productDao.getById(conn, item.getProductId());
                if (product == null || product.getStock() < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for product ID: " + item.getProductId());
                }
            }

            // Insert Sale + SaleItems
            saleDao.insertSale(conn, sale);

            // Process movements and stock updates
            for (SaleItem item : sale.getItems()) {
                // Update stock
                productDao.updateStock(conn, item.getProductId(), -item.getQuantity());

                // Create InventoryMovement
                InventoryMovement movement = new InventoryMovement();
                movement.setProductId(item.getProductId());
                movement.setQuantity(-item.getQuantity());
                movement.setType("SALE");
                movement.setCreatedAt(LocalDateTime.now());
                movement.setReason("Sale #" + sale.getSaleNumber());
                inventoryMovementDao.insert(conn, movement);
            }

            // If payment is EFECTIVO, create CashMovement
            if ("EFECTIVO".equalsIgnoreCase(sale.getPaymentMethod())) {
                CashMovement cashMov = new CashMovement();
                cashMov.setCashSessionId(sale.getCashSessionId());
                cashMov.setAmount(sale.getTotal());
                cashMov.setType("SALE");
                cashMov.setCreatedAt(LocalDateTime.now());
                cashMov.setReason("Sale #" + sale.getSaleNumber());
                cashMov.setRelatedSaleId(sale.getId());
                cashMovementDao.insert(conn, cashMov);
            }

            conn.commit();
            logger.info("Successfully completed sale: {}", sale.getSaleNumber());
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction for sale: {}", sale.getSaleNumber(), ex);
                }
            }
            logger.error("Failed to complete sale: {}", sale.getSaleNumber(), e);
            throw new RuntimeException("Failed to complete sale: " + e.getMessage(), e);
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

    public Sale getSaleByNumber(String saleNumber) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return saleDao.getBySaleNumber(conn, saleNumber);
        } catch (SQLException e) {
            logger.error("Error retrieving sale by number: {}", saleNumber, e);
            throw new RuntimeException("Database error retrieving sale", e);
        }
    }

    public List<Sale> getSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return saleDao.getByDateRange(conn, start, end);
        } catch (SQLException e) {
            logger.error("Error retrieving sales by date range", e);
            throw new RuntimeException("Database error retrieving sales", e);
        }
    }
}

