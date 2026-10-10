package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;
import com.papeleria.pos.dao.ReturnDao;
import com.papeleria.pos.dao.SaleDao;
import com.papeleria.pos.dao.InventoryMovementDao;
import com.papeleria.pos.dao.CashMovementDao;
import com.papeleria.pos.dao.CashSessionDao;
import com.papeleria.pos.dao.ProductDao;
import com.papeleria.pos.model.SaleReturn;
import com.papeleria.pos.model.ReturnItem;
import com.papeleria.pos.model.Sale;
import com.papeleria.pos.model.InventoryMovement;
import com.papeleria.pos.model.CashMovement;
import com.papeleria.pos.model.CashSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing sale returns and refunds.
 */
public class ReturnService {
    private static final Logger logger = LoggerFactory.getLogger(ReturnService.class);

    private final ReturnDao returnDao = new ReturnDao();
    private final SaleDao saleDao = new SaleDao();
    private final InventoryMovementDao inventoryMovementDao = new InventoryMovementDao();
    private final CashMovementDao cashMovementDao = new CashMovementDao();
    private final CashSessionDao cashSessionDao = new CashSessionDao();
    private final ProductDao productDao = new ProductDao();

    /**
     * Processes a sale return.
     * Validates sale status, inserts return and items, restores inventory,
     * and processes cash refund if applicable.
     *
     * @param returnObj the SaleReturn object with items to process
     */
    public void processReturn(SaleReturn returnObj) {
        logger.info("Processing return for sale ID: {}", returnObj.getSaleId());
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getTransactionConnection();
            
            Sale sale = saleDao.getById(conn, returnObj.getSaleId());
            if (sale == null) {
                throw new IllegalArgumentException("Sale not found with ID: " + returnObj.getSaleId());
            }
            
            if (!"COMPLETED".equalsIgnoreCase(sale.getStatus())) {
                throw new IllegalStateException("Cannot process return for a sale with status: " + sale.getStatus());
            }

            returnObj.setCreatedAt(LocalDateTime.now());
            returnDao.insertReturn(conn, returnObj);

            if (returnObj.getItems() != null) {
                for (ReturnItem item : returnObj.getItems()) {
                    // Restore stock
                    productDao.updateStock(conn, item.getProductId(), item.getQuantity());

                    InventoryMovement invMovement = new InventoryMovement();
                    invMovement.setProductId(item.getProductId());
                    invMovement.setQuantity(item.getQuantity());
                    invMovement.setType("RETURN");
                    invMovement.setReason("Return for Sale ID: " + sale.getId());
                    invMovement.setCreatedAt(LocalDateTime.now());
                    inventoryMovementDao.insert(conn, invMovement);
                }
            }

            if ("EFECTIVO".equalsIgnoreCase(sale.getPaymentMethod())) {
                CashSession session = cashSessionDao.getActiveSession(conn);
                if (session == null) {
                    throw new IllegalStateException("No active cash session to register refund.");
                }
                
                CashMovement cashMovement = new CashMovement();
                cashMovement.setCashSessionId(session.getId());
                cashMovement.setAmount(returnObj.getTotal());
                cashMovement.setType("REFUND");
                cashMovement.setReason("Refund for Sale ID: " + sale.getId());
                cashMovement.setCreatedAt(LocalDateTime.now());
                cashMovement.setRelatedReturnId(returnObj.getId());
                
                cashMovementDao.insert(conn, cashMovement);
            }

            conn.commit();
            logger.info("Return processed successfully for sale ID: {}", sale.getId());
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction for return", ex);
                }
            }
            logger.error("Failed to process return", e);
            throw new RuntimeException("Failed to process return: " + e.getMessage(), e);
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

    /**
     * Retrieves all returns associated with a specific sale ID.
     *
     * @param saleId the sale ID
     * @return a list of SaleReturn objects
     */
    public List<SaleReturn> getReturnsBySaleId(int saleId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return returnDao.getBySaleId(conn, saleId);
        } catch (SQLException e) {
            logger.error("Error retrieving returns for sale ID: {}", saleId, e);
            throw new RuntimeException("Failed to retrieve returns", e);
        }
    }
}
