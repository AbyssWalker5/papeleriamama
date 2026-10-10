package com.papeleria.pos.dao;

import com.papeleria.pos.model.InventoryMovement;
import com.papeleria.pos.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class InventoryMovementDao {
    private static final Logger logger = LoggerFactory.getLogger(InventoryMovementDao.class);

    public void insert(Connection conn, InventoryMovement movement) {
        String sql = "INSERT INTO inventory_movements (product_id, type, quantity, previous_stock, new_stock, reason, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, movement.getProductId());
            stmt.setString(2, movement.getType());
            stmt.setInt(3, movement.getQuantity());
            stmt.setInt(4, movement.getPreviousStock());
            stmt.setInt(5, movement.getNewStock());
            stmt.setString(6, movement.getReason());
            stmt.setString(7, DateUtil.toDbString(movement.getCreatedAt()));
            
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    movement.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting inventory movement", e);
            throw new RuntimeException("Error inserting inventory movement", e);
        }
    }

    public List<InventoryMovement> getByProductId(Connection conn, int productId) {
        String sql = "SELECT id, product_id, type, quantity, previous_stock, new_stock, reason, created_at " +
                     "FROM inventory_movements WHERE product_id = ? ORDER BY created_at DESC";
        List<InventoryMovement> movements = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movements.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching movements for product_id: {}", productId, e);
            throw new RuntimeException("Error fetching movements", e);
        }
        return movements;
    }

    private InventoryMovement mapRow(ResultSet rs) throws SQLException {
        InventoryMovement movement = new InventoryMovement();
        movement.setId(rs.getInt("id"));
        movement.setProductId(rs.getInt("product_id"));
        movement.setType(rs.getString("type"));
        movement.setQuantity(rs.getInt("quantity"));
        movement.setPreviousStock(rs.getInt("previous_stock"));
        movement.setNewStock(rs.getInt("new_stock"));
        movement.setReason(rs.getString("reason"));
        movement.setCreatedAt(DateUtil.fromDbString(rs.getString("created_at")));
        return movement;
    }
}
