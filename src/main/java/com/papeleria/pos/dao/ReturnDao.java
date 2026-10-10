package com.papeleria.pos.dao;

import com.papeleria.pos.model.ReturnItem;
import com.papeleria.pos.model.SaleReturn;
import com.papeleria.pos.util.DateUtil;
import com.papeleria.pos.util.MoneyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for SaleReturn entities.
 */
public class ReturnDao {
    private static final Logger logger = LoggerFactory.getLogger(ReturnDao.class);

    public void insertReturn(Connection conn, SaleReturn saleReturn) throws SQLException {
        String sql = "INSERT INTO returns (sale_id, return_date, total_amount, reason, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, saleReturn.getSaleId());
            pstmt.setString(2, saleReturn.getCreatedAt() != null ? DateUtil.toDbString(saleReturn.getCreatedAt()) : null);
            pstmt.setInt(3, MoneyUtil.toCents(saleReturn.getTotal()));
            pstmt.setString(4, saleReturn.getReason());
            pstmt.setString(5, saleReturn.getStatus());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    saleReturn.setId(rs.getInt(1));
                }
            }
        }
        
        if (saleReturn.getItems() != null && !saleReturn.getItems().isEmpty()) {
            String itemSql = "INSERT INTO return_items (return_id, sale_item_id, quantity, refund_amount) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
                for (ReturnItem item : saleReturn.getItems()) {
                    pstmt.setInt(1, saleReturn.getId());
                    pstmt.setInt(2, item.getSaleItemId());
                    pstmt.setInt(3, item.getQuantity());
                    pstmt.setInt(4, MoneyUtil.toCents(item.getSubtotal()));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    int i = 0;
                    while (rs.next() && i < saleReturn.getItems().size()) {
                        saleReturn.getItems().get(i).setId(rs.getInt(1));
                        i++;
                    }
                }
            }
        }
    }

    public List<SaleReturn> getBySaleId(Connection conn, int saleId) throws SQLException {
        String sql = "SELECT * FROM returns WHERE sale_id = ? ORDER BY return_date DESC";
        List<SaleReturn> returns = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SaleReturn r = mapRowToReturn(rs);
                    r.setItems(getItemsByReturnId(conn, r.getId()));
                    returns.add(r);
                }
            }
        }
        return returns;
    }

    public SaleReturn getById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM returns WHERE id = ?";
        SaleReturn saleReturn = null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    saleReturn = mapRowToReturn(rs);
                }
            }
        }
        
        if (saleReturn != null) {
            saleReturn.setItems(getItemsByReturnId(conn, saleReturn.getId()));
        }
        
        return saleReturn;
    }
    
    private List<ReturnItem> getItemsByReturnId(Connection conn, int returnId) throws SQLException {
        String sql = "SELECT * FROM return_items WHERE return_id = ?";
        List<ReturnItem> items = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, returnId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ReturnItem item = new ReturnItem();
                    item.setId(rs.getInt("id"));
                    item.setReturnId(rs.getInt("return_id"));
                    item.setSaleItemId(rs.getInt("sale_item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSubtotal(MoneyUtil.fromCents(rs.getInt("refund_amount")));
                    items.add(item);
                }
            }
        }
        return items;
    }
    
    private SaleReturn mapRowToReturn(ResultSet rs) throws SQLException {
        SaleReturn r = new SaleReturn();
        r.setId(rs.getInt("id"));
        r.setSaleId(rs.getInt("sale_id"));
        
        String dateStr = rs.getString("return_date");
        if (dateStr != null) {
            r.setCreatedAt(DateUtil.fromDbString(dateStr));
        }
        
        r.setTotal(MoneyUtil.fromCents(rs.getInt("total_amount")));
        r.setReason(rs.getString("reason"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}


