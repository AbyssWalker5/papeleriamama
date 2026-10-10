package com.papeleria.pos.dao;

import com.papeleria.pos.model.Sale;
import com.papeleria.pos.model.SaleItem;
import com.papeleria.pos.util.DateUtil;
import com.papeleria.pos.util.MoneyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Sale and SaleItem entities.
 */
public class SaleDao {
    private static final Logger logger = LoggerFactory.getLogger(SaleDao.class);

    public boolean existsByIdempotencyToken(Connection conn, String token) throws SQLException {
        String sql = "SELECT 1 FROM sales WHERE idempotency_token = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insertSale(Connection conn, Sale sale) throws SQLException {
        String sql = "INSERT INTO sales (sale_number, idempotency_token, cash_session_id, subtotal_cents, discount_cents, total_cents, payment_method, amount_paid_cents, change_cents, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, sale.getSaleNumber());
            pstmt.setString(2, sale.getIdempotencyToken());
            pstmt.setInt(3, sale.getCashSessionId());
            pstmt.setInt(4, MoneyUtil.toCents(sale.getSubtotal()));
            pstmt.setInt(5, MoneyUtil.toCents(sale.getDiscount()));
            pstmt.setInt(6, MoneyUtil.toCents(sale.getTotal()));
            pstmt.setString(7, sale.getPaymentMethod());
            if (sale.getAmountPaid() != null) {
                pstmt.setInt(8, MoneyUtil.toCents(sale.getAmountPaid()));
            } else {
                pstmt.setNull(8, java.sql.Types.INTEGER);
            }
            if (sale.getChange() != null) {
                pstmt.setInt(9, MoneyUtil.toCents(sale.getChange()));
            } else {
                pstmt.setInt(9, 0);
            }
            pstmt.setString(10, sale.getStatus());
            pstmt.setString(11, sale.getCreatedAt() != null ? DateUtil.toDbString(sale.getCreatedAt()) : DateUtil.toDbString(DateUtil.now()));
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    sale.setId(rs.getInt(1));
                }
            }
        }
        
        if (sale.getItems() != null && !sale.getItems().isEmpty()) {
            String itemSql = "INSERT INTO sale_items (sale_id, product_id, product_name_snapshot, product_barcode_snapshot, unit_price_cents_snapshot, quantity, discount_cents, subtotal_cents) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(itemSql, Statement.RETURN_GENERATED_KEYS)) {
                for (SaleItem item : sale.getItems()) {
                    pstmt.setInt(1, sale.getId());
                    pstmt.setInt(2, item.getProductId());
                    pstmt.setString(3, item.getProductNameSnapshot());
                    pstmt.setString(4, item.getProductBarcodeSnapshot());
                    pstmt.setInt(5, MoneyUtil.toCents(item.getUnitPriceSnapshot()));
                    pstmt.setInt(6, item.getQuantity());
                    pstmt.setInt(7, MoneyUtil.toCents(item.getDiscount()));
                    pstmt.setInt(8, MoneyUtil.toCents(item.getSubtotal()));
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
                
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    int i = 0;
                    while (rs.next() && i < sale.getItems().size()) {
                        sale.getItems().get(i).setId(rs.getInt(1));
                        i++;
                    }
                }
            }
        }
    }

    public Sale getById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM sales WHERE id = ?";
        Sale sale = null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    sale = mapRowToSale(rs);
                }
            }
        }
        
        if (sale != null) {
            sale.setItems(getItemsBySaleId(conn, id));
        }
        
        return sale;
    }

    public Sale getBySaleNumber(Connection conn, String saleNumber) throws SQLException {
        String sql = "SELECT * FROM sales WHERE sale_number = ?";
        Sale sale = null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, saleNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    sale = mapRowToSale(rs);
                }
            }
        }
        
        if (sale != null) {
            sale.setItems(getItemsBySaleId(conn, sale.getId()));
        }
        
        return sale;
    }

    public List<Sale> getByDateRange(Connection conn, LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = "SELECT * FROM sales WHERE created_at >= ? AND created_at <= ? ORDER BY created_at DESC";
        List<Sale> sales = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, DateUtil.toDbString(start));
            pstmt.setString(2, DateUtil.toDbString(end));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapRowToSale(rs);
                    sale.setItems(getItemsBySaleId(conn, sale.getId()));
                    sales.add(sale);
                }
            }
        }
        return sales;
    }

    public void updateStatus(Connection conn, int saleId, String status) throws SQLException {
        String sql = "UPDATE sales SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, saleId);
            pstmt.executeUpdate();
        }
    }
    
    private List<SaleItem> getItemsBySaleId(Connection conn, int saleId) throws SQLException {
        String sql = "SELECT * FROM sale_items WHERE sale_id = ?";
        List<SaleItem> items = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, saleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setId(rs.getInt("id"));
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductNameSnapshot(rs.getString("product_name_snapshot"));
                    item.setProductBarcodeSnapshot(rs.getString("product_barcode_snapshot"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPriceSnapshot(MoneyUtil.fromCents(rs.getInt("unit_price_cents_snapshot")));
                    item.setDiscount(MoneyUtil.fromCents(rs.getInt("discount_cents")));
                    item.setSubtotal(MoneyUtil.fromCents(rs.getInt("subtotal_cents")));
                    items.add(item);
                }
            }
        }
        return items;
    }
    
    private Sale mapRowToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setId(rs.getInt("id"));
        sale.setSaleNumber(rs.getString("sale_number"));
        sale.setIdempotencyToken(rs.getString("idempotency_token"));
        sale.setCashSessionId(rs.getInt("cash_session_id"));
        String dateStr = rs.getString("created_at");
        if (dateStr != null) {
            sale.setCreatedAt(DateUtil.fromDbString(dateStr));
        }
        sale.setSubtotal(MoneyUtil.fromCents(rs.getInt("subtotal_cents")));
        sale.setDiscount(MoneyUtil.fromCents(rs.getInt("discount_cents")));
        sale.setTotal(MoneyUtil.fromCents(rs.getInt("total_cents")));
        sale.setStatus(rs.getString("status"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        
        int paid = rs.getInt("amount_paid_cents");
        if (!rs.wasNull()) {
            sale.setAmountPaid(MoneyUtil.fromCents(paid));
        }
        
        sale.setChange(MoneyUtil.fromCents(rs.getInt("change_cents")));
        return sale;
    }
}
