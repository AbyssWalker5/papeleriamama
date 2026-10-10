package com.papeleria.pos.dao;

import com.papeleria.pos.model.CashClosure;
import com.papeleria.pos.model.CashClosureCorrection;
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
 * Data Access Object for CashClosure entities.
 */
public class CashClosureDao {
    private static final Logger logger = LoggerFactory.getLogger(CashClosureDao.class);

    public void insert(Connection conn, CashClosure closure) throws SQLException {
        String sql = "INSERT INTO cash_closures (cash_session_id, initial_fund_cents, cash_sales_cents, " +
                "card_sales_cents, transfer_sales_cents, other_sales_cents, total_sales_cents, total_transactions, " +
                "cash_in_cents, cash_out_cents, returns_cents, expected_cash_cents, counted_cash_cents, " +
                "difference_cents, observations, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, closure.getCashSessionId());
            pstmt.setInt(2, MoneyUtil.toCents(closure.getInitialFund()));
            pstmt.setInt(3, MoneyUtil.toCents(closure.getCashSales()));
            pstmt.setInt(4, MoneyUtil.toCents(closure.getCardSales()));
            pstmt.setInt(5, MoneyUtil.toCents(closure.getTransferSales()));
            pstmt.setInt(6, MoneyUtil.toCents(closure.getOtherSales()));
            pstmt.setInt(7, MoneyUtil.toCents(closure.getTotalSales()));
            pstmt.setInt(8, closure.getTotalTransactions());
            pstmt.setInt(9, MoneyUtil.toCents(closure.getCashIn()));
            pstmt.setInt(10, MoneyUtil.toCents(closure.getCashOut()));
            pstmt.setInt(11, MoneyUtil.toCents(closure.getReturns()));
            pstmt.setInt(12, MoneyUtil.toCents(closure.getExpectedCash()));
            pstmt.setInt(13, MoneyUtil.toCents(closure.getCountedCash()));
            pstmt.setInt(14, MoneyUtil.toCents(closure.getDifference()));
            pstmt.setString(15, closure.getObservations());
            pstmt.setString(16, closure.getCreatedAt() != null ? DateUtil.toDbString(closure.getCreatedAt()) : DateUtil.toDbString(DateUtil.now()));
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    closure.setId(rs.getInt(1));
                }
            }
        }
    }

    public CashClosure getBySessionId(Connection conn, int sessionId) throws SQLException {
        String sql = "SELECT * FROM cash_closures WHERE cash_session_id = ? ORDER BY created_at DESC LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sessionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToClosure(rs);
                }
            }
        }
        return null;
    }

    public void insertCorrection(Connection conn, CashClosureCorrection correction) throws SQLException {
        String sql = "INSERT INTO cash_closure_corrections (cash_closure_id, field_corrected, old_value, new_value, justification, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, correction.getCashClosureId());
            pstmt.setString(2, correction.getFieldCorrected());
            pstmt.setString(3, correction.getOldValue());
            pstmt.setString(4, correction.getNewValue());
            pstmt.setString(5, correction.getJustification());
            pstmt.setString(6, correction.getCreatedAt() != null ? DateUtil.toDbString(correction.getCreatedAt()) : DateUtil.toDbString(DateUtil.now()));
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    correction.setId(rs.getInt(1));
                }
            }
        }
    }
    
    private CashClosure mapRowToClosure(ResultSet rs) throws SQLException {
        CashClosure c = new CashClosure();
        c.setId(rs.getInt("id"));
        c.setCashSessionId(rs.getInt("cash_session_id"));
        c.setInitialFund(MoneyUtil.fromCents(rs.getInt("initial_fund_cents")));
        c.setCashSales(MoneyUtil.fromCents(rs.getInt("cash_sales_cents")));
        c.setCardSales(MoneyUtil.fromCents(rs.getInt("card_sales_cents")));
        c.setTransferSales(MoneyUtil.fromCents(rs.getInt("transfer_sales_cents")));
        c.setOtherSales(MoneyUtil.fromCents(rs.getInt("other_sales_cents")));
        c.setTotalSales(MoneyUtil.fromCents(rs.getInt("total_sales_cents")));
        c.setTotalTransactions(rs.getInt("total_transactions"));
        c.setCashIn(MoneyUtil.fromCents(rs.getInt("cash_in_cents")));
        c.setCashOut(MoneyUtil.fromCents(rs.getInt("cash_out_cents")));
        c.setReturns(MoneyUtil.fromCents(rs.getInt("returns_cents")));
        c.setExpectedCash(MoneyUtil.fromCents(rs.getInt("expected_cash_cents")));
        c.setCountedCash(MoneyUtil.fromCents(rs.getInt("counted_cash_cents")));
        c.setDifference(MoneyUtil.fromCents(rs.getInt("difference_cents")));
        c.setObservations(rs.getString("observations"));
        
        String timeStr = rs.getString("created_at");
        if (timeStr != null) {
            c.setCreatedAt(DateUtil.fromDbString(timeStr));
        }
        return c;
    }
}
