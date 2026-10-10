package com.papeleria.pos.dao;

import com.papeleria.pos.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDao.class);

    public List<Category> getAll(Connection conn) {
        String sql = "SELECT id, name, description, active FROM categories WHERE active = 1";
        List<Category> categories = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categories.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all categories", e);
            throw new RuntimeException("Error fetching all categories", e);
        }
        return categories;
    }

    public Category getById(Connection conn, int id) {
        String sql = "SELECT id, name, description, active FROM categories WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching category by id: {}", id, e);
            throw new RuntimeException("Error fetching category by id", e);
        }
        return null;
    }

    public void insert(Connection conn, Category category) {
        String sql = "INSERT INTO categories (name, description, active) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.isActive() ? 1 : 0);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting category", e);
            throw new RuntimeException("Error inserting category", e);
        }
    }

    public void update(Connection conn, Category category) {
        String sql = "UPDATE categories SET name = ?, description = ?, active = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.isActive() ? 1 : 0);
            stmt.setInt(4, category.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating category: {}", category.getId(), e);
            throw new RuntimeException("Error updating category", e);
        }
    }

    public void delete(Connection conn, int id) {
        String sql = "UPDATE categories SET active = 0 WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting (deactivating) category: {}", id, e);
            throw new RuntimeException("Error deleting category", e);
        }
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setActive(rs.getInt("active") == 1);
        return category;
    }
}


