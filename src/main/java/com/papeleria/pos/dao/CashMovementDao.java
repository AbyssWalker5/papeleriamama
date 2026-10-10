package com.papeleria.pos.dao;

import com.papeleria.pos.model.CashMovement;
import com.papeleria.pos.util.DateUtil;
import com.papeleria.pos.util.MoneyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for CashMovement entities.
 */
public class CashMovementDao {
    private static final Logger logger = LoggerFactory.getLogger(CashMovementDao.class);

    public void insert(Connection conn, CashMovement movement) throws SQLException {
        String sql = "INSERT INTO cash_movements (cash_session_id, type, amount_cents, reason, related_sale_id, related_return_id, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, movement.getCashSessionId());
            pstmt.setString(2, movement.getType());
            pstmt.setInt(3, MoneyUtil.toCents(movement.getAmount()));
            pstmt.setString(4, movement.getReason());
            if (movement.getRelatedSaleId() != null) {
                pstmt.setInt(5, movement.getRelatedSaleId());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            if (movement.getRelatedReturnId() != null) {
                pstmt.setInt(6, movement.getRelatedReturnId());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            pstmt.setString(7, movement.getCreatedAt() != null ? DateUtil.toDbString(movement.getCreatedAt()) : DateUtil.toDbString(DateUtil.now()));
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    movement.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<CashMovement> getBySessionId(Connection conn, int sessionId) throws SQLException {
        String sql = "SELECT * FROM cash_movements WHERE cash_session_id = ? ORDER BY created_at ASC";
        List<CashMovement> movements = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movements.add(mapRowToMovement(rs));
                }
            }
        }
        return movements;
    }
    
    private CashMovement mapRowToMovement(ResultSet rs) throws SQLException {
        CashMovement m = new CashMovement();
        m.setId(rs.getInt("id"));
        m.setCashSessionId(rs.getInt("cash_session_id"));
        m.setType(rs.getString("type"));
        m.setAmount(MoneyUtil.fromCents(rs.getInt("amount_cents")));
        m.setReason(rs.getString("reason"));
        
        int saleId = rs.getInt("related_sale_id");
        if (!rs.wasNull()) {
            m.setRelatedSaleId(saleId);
        }
        
        int returnId = rs.getInt("related_return_id");
        if (!rs.wasNull()) {
            m.setRelatedReturnId(returnId);
        }
        
        String timeStr = rs.getString("created_at");
        if (timeStr != null) {
            m.setCreatedAt(DateUtil.fromDbString(timeStr));
        }
        return m;
    }
}
