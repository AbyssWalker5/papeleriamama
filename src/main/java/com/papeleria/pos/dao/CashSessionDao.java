package com.papeleria.pos.dao;

import com.papeleria.pos.model.CashSession;
import com.papeleria.pos.util.DateUtil;
import com.papeleria.pos.util.MoneyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object for CashSession entities.
 */
public class CashSessionDao {
    private static final Logger logger = LoggerFactory.getLogger(CashSessionDao.class);

    public void insert(Connection conn, CashSession session) throws SQLException {
        String sql = "INSERT INTO cash_sessions (initial_fund_cents, status, opened_at, closed_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, MoneyUtil.toCents(session.getInitialFund()));
            pstmt.setString(2, session.getStatus());
            pstmt.setString(3, session.getOpenedAt() != null ? DateUtil.toDbString(session.getOpenedAt()) : DateUtil.toDbString(DateUtil.now()));
            pstmt.setString(4, session.getClosedAt() != null ? DateUtil.toDbString(session.getClosedAt()) : null);
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    session.setId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Connection conn, CashSession session) throws SQLException {
        String sql = "UPDATE cash_sessions SET initial_fund_cents = ?, status = ?, opened_at = ?, closed_at = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, MoneyUtil.toCents(session.getInitialFund()));
            pstmt.setString(2, session.getStatus());
            pstmt.setString(3, session.getOpenedAt() != null ? DateUtil.toDbString(session.getOpenedAt()) : null);
            pstmt.setString(4, session.getClosedAt() != null ? DateUtil.toDbString(session.getClosedAt()) : null);
            pstmt.setInt(5, session.getId());
            
            pstmt.executeUpdate();
        }
    }

    public CashSession getActiveSession(Connection conn) throws SQLException {
        String sql = "SELECT * FROM cash_sessions WHERE status = 'OPEN' ORDER BY opened_at DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return mapRowToSession(rs);
            }
        }
        return null;
    }

    public CashSession getById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM cash_sessions WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSession(rs);
                }
            }
        }
        return null;
    }
    
    private CashSession mapRowToSession(ResultSet rs) throws SQLException {
        CashSession session = new CashSession();
        session.setId(rs.getInt("id"));
        session.setInitialFund(MoneyUtil.fromCents(rs.getInt("initial_fund_cents")));
        session.setStatus(rs.getString("status"));
        
        String startStr = rs.getString("opened_at");
        if (startStr != null) {
            session.setOpenedAt(DateUtil.fromDbString(startStr));
        }
        
        String endStr = rs.getString("closed_at");
        if (endStr != null) {
            session.setClosedAt(DateUtil.fromDbString(endStr));
        }
        
        return session;
    }
}
