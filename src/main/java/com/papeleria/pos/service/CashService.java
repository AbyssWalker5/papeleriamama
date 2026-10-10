package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;
import com.papeleria.pos.dao.CashClosureDao;
import com.papeleria.pos.dao.CashMovementDao;
import com.papeleria.pos.dao.CashSessionDao;
import com.papeleria.pos.dao.SaleDao;
import com.papeleria.pos.model.CashClosure;
import com.papeleria.pos.model.CashMovement;
import com.papeleria.pos.model.CashSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class CashService {
    private static final Logger logger = LoggerFactory.getLogger(CashService.class);

    private final CashSessionDao cashSessionDAO = new CashSessionDao();
    private final CashMovementDao cashMovementDAO = new CashMovementDao();
    private final CashClosureDao cashClosureDAO = new CashClosureDao();
    private final SaleDao saleDAO = new SaleDao();

    public CashService() {
    }

    public boolean isSessionOpen() {
        return getActiveSession() != null;
    }

    public List<CashMovement> getCurrentSessionMovements() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            CashSession session = cashSessionDAO.getActiveSession(conn);
            if (session != null) {
                return cashMovementDAO.getBySessionId(conn, session.getId());
            }
            return List.of();
        } catch (SQLException e) {
            logger.error("Error retrieving current session movements", e);
            throw new RuntimeException("Failed to get session movements", e);
        }
    }

    public void openSession(BigDecimal initialFund) {
        logger.info("Opening cash session with initial fund: {}", initialFund);
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            CashSession activeSession = cashSessionDAO.getActiveSession(conn);
            if (activeSession != null) {
                throw new IllegalStateException("A cash session is already open.");
            }

            CashSession session = new CashSession();
            session.setOpenedAt(LocalDateTime.now());
            session.setInitialFund(initialFund);
            session.setStatus("OPEN");
            
            cashSessionDAO.insert(conn, session);
            logger.info("Cash session opened successfully.");
        } catch (SQLException e) {
            logger.error("Error opening cash session", e);
            throw new RuntimeException("Failed to open cash session", e);
        }
    }

    public CashSession getActiveSession() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return cashSessionDAO.getActiveSession(conn);
        } catch (SQLException e) {
            logger.error("Error retrieving active session", e);
            throw new RuntimeException("Failed to get active session", e);
        }
    }

    public void addCash(BigDecimal amount, String reason) {
        logger.info("Adding cash: {} - Reason: {}", amount, reason);
        registerMovement(amount, reason, "CASH_IN");
    }

    public void removeCash(BigDecimal amount, String reason) {
        logger.info("Removing cash: {} - Reason: {}", amount, reason);
        registerMovement(amount, reason, "CASH_OUT");
    }

    public void registerIncome(BigDecimal amount, String description) {
        addCash(amount, description);
    }

    public void registerExpense(BigDecimal amount, String description) {
        removeCash(amount, description);
    }

    private void registerMovement(BigDecimal amount, String reason, String type) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            CashSession session = cashSessionDAO.getActiveSession(conn);
            if (session == null) {
                throw new IllegalStateException("No active cash session found.");
            }

            CashMovement movement = new CashMovement();
            movement.setCashSessionId(session.getId());
            movement.setAmount(amount);
            movement.setReason(reason);
            movement.setType(type);
            movement.setCreatedAt(LocalDateTime.now());

            cashMovementDAO.insert(conn, movement);
        } catch (SQLException e) {
            logger.error("Error registering cash movement: {}", type, e);
            throw new RuntimeException("Failed to register cash movement", e);
        }
    }

    public BigDecimal getExpectedCash() {
        // Simple implementation since saleDAO.getTotalSalesByPaymentMethod is missing.
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            CashSession session = cashSessionDAO.getActiveSession(conn);
            if (session == null) {
                throw new IllegalStateException("No active cash session found.");
            }

            BigDecimal total = session.getInitialFund() != null ? session.getInitialFund() : BigDecimal.ZERO;
            List<CashMovement> movements = cashMovementDAO.getBySessionId(conn, session.getId());
            for (CashMovement m : movements) {
                if ("SALE".equals(m.getType()) || "CASH_IN".equals(m.getType())) {
                    total = total.add(m.getAmount());
                } else if ("CASH_OUT".equals(m.getType()) || "REFUND".equals(m.getType())) {
                    total = total.subtract(m.getAmount());
                }
            }
            return total;
        } catch (SQLException e) {
            logger.error("Error calculating expected cash", e);
            throw new RuntimeException("Failed to calculate expected cash", e);
        }
    }

    public void closeSession() {
        closeSession(getExpectedCash(), "Cierre normal");
    }

    public void closeSession(BigDecimal countedCash, String observations) {
        logger.info("Closing cash session. Counted cash: {}", countedCash);
        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getTransactionConnection();
            
            CashSession session = cashSessionDAO.getActiveSession(conn);
            if (session == null) {
                throw new IllegalStateException("No active cash session found to close.");
            }

            BigDecimal expectedCash = getExpectedCash(); // Note: we could do it in same transaction, but fine for now
            BigDecimal difference = countedCash.subtract(expectedCash);

            CashClosure closure = new CashClosure();
            closure.setCashSessionId(session.getId());
            closure.setInitialFund(session.getInitialFund());
            
            // Dummy zeros for closure
            closure.setCashSales(BigDecimal.ZERO);
            closure.setCardSales(BigDecimal.ZERO);
            closure.setTransferSales(BigDecimal.ZERO);
            closure.setOtherSales(BigDecimal.ZERO);
            closure.setTotalSales(BigDecimal.ZERO);
            closure.setTotalTransactions(0);
            closure.setCashIn(BigDecimal.ZERO);
            closure.setCashOut(BigDecimal.ZERO);
            closure.setReturns(BigDecimal.ZERO);
            
            closure.setExpectedCash(expectedCash);
            closure.setCountedCash(countedCash);
            closure.setDifference(difference);
            closure.setObservations(observations);
            closure.setCreatedAt(LocalDateTime.now());

            cashClosureDAO.insert(conn, closure);

            session.setStatus("CLOSED");
            session.setClosedAt(LocalDateTime.now());
            cashSessionDAO.update(conn, session);

            conn.commit();
            logger.info("Cash session closed successfully. Expected: {}, Counted: {}, Difference: {}", expectedCash, countedCash, difference);
        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ex) {}
            }
            logger.error("Error closing cash session, transaction rolled back", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ex) {}
            }
        }
    }
}
