package com.papeleria.pos.dao;

import com.papeleria.pos.model.Product;
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

public class ProductDao {
    private static final Logger logger = LoggerFactory.getLogger(ProductDao.class);

    public List<Product> getAll(Connection conn) {
        String sql = "SELECT * FROM products WHERE active = 1";
        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all products", e);
            throw new RuntimeException("Error fetching all products", e);
        }
        return products;
    }

    public Product getById(Connection conn, int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching product by id: {}", id, e);
            throw new RuntimeException("Error fetching product by id", e);
        }
        return null;
    }

    public Product getByBarcode(Connection conn, String barcode) {
        String sql = "SELECT * FROM products WHERE barcode = ? AND active = 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, barcode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error fetching product by barcode: {}", barcode, e);
            throw new RuntimeException("Error fetching product by barcode", e);
        }
        return null;
    }

    public void insert(Connection conn, Product product) {
        String sql = "INSERT INTO products (barcode, sku, name, description, category_id, purchase_price_cents, sale_price_cents, stock, min_stock, unit, active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getSku());
            stmt.setString(3, product.getName());
            stmt.setString(4, product.getDescription());
            if (product.getCategoryId() > 0) {
                stmt.setInt(5, product.getCategoryId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setInt(6, MoneyUtil.toCents(product.getPurchasePrice()));
            stmt.setInt(7, MoneyUtil.toCents(product.getSalePrice()));
            stmt.setInt(8, product.getStock());
            stmt.setInt(9, product.getMinStock());
            stmt.setString(10, product.getUnit());
            stmt.setInt(11, product.isActive() ? 1 : 0);
            stmt.setString(12, product.getCreatedAt() != null ? DateUtil.toDbString(product.getCreatedAt()) : DateUtil.toDbString(DateUtil.now()));
            stmt.setString(13, product.getUpdatedAt() != null ? DateUtil.toDbString(product.getUpdatedAt()) : DateUtil.toDbString(DateUtil.now()));
            
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting product", e);
            throw new RuntimeException("Error inserting product", e);
        }
    }

    public void update(Connection conn, Product product) {
        String sql = "UPDATE products SET barcode = ?, sku = ?, name = ?, description = ?, category_id = ?, " +
                     "purchase_price_cents = ?, sale_price_cents = ?, stock = ?, min_stock = ?, unit = ?, active = ?, updated_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getBarcode());
            stmt.setString(2, product.getSku());
            stmt.setString(3, product.getName());
            stmt.setString(4, product.getDescription());
            if (product.getCategoryId() > 0) {
                stmt.setInt(5, product.getCategoryId());
            } else {
                stmt.setNull(5, java.sql.Types.INTEGER);
            }
            stmt.setInt(6, MoneyUtil.toCents(product.getPurchasePrice()));
            stmt.setInt(7, MoneyUtil.toCents(product.getSalePrice()));
            stmt.setInt(8, product.getStock());
            stmt.setInt(9, product.getMinStock());
            stmt.setString(10, product.getUnit());
            stmt.setInt(11, product.isActive() ? 1 : 0);
            stmt.setString(12, product.getUpdatedAt() != null ? DateUtil.toDbString(product.getUpdatedAt()) : DateUtil.toDbString(DateUtil.now()));
            stmt.setInt(13, product.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating product: {}", product.getId(), e);
            throw new RuntimeException("Error updating product", e);
        }
    }

    public void updateStock(Connection conn, int productId, int quantityChange) {
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantityChange);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating stock for product: {}", productId, e);
            throw new RuntimeException("Error updating stock for product", e);
        }
    }

    public List<Product> search(Connection conn, String term) {
        String sql = "SELECT * FROM products WHERE (name LIKE ? OR barcode LIKE ? OR sku LIKE ?) AND active = 1";
        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            String likeTerm = "%" + term + "%";
            stmt.setString(1, likeTerm);
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error searching products with term: {}", term, e);
            throw new RuntimeException("Error searching products", e);
        }
        return products;
    }

    public List<Product> getLowStock(Connection conn) {
        String sql = "SELECT * FROM products WHERE stock <= min_stock AND active = 1";
        List<Product> products = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching low stock products", e);
            throw new RuntimeException("Error fetching low stock products", e);
        }
        return products;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setBarcode(rs.getString("barcode"));
        product.setSku(rs.getString("sku"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        
        int categoryId = rs.getInt("category_id");
        if (!rs.wasNull()) {
            product.setCategoryId(categoryId);
        }
        
        product.setPurchasePrice(MoneyUtil.fromCents(rs.getInt("purchase_price_cents")));
        product.setSalePrice(MoneyUtil.fromCents(rs.getInt("sale_price_cents")));
        product.setStock(rs.getInt("stock"));
        product.setMinStock(rs.getInt("min_stock"));
        product.setUnit(rs.getString("unit"));
        product.setActive(rs.getInt("active") == 1);
        
        String createdStr = rs.getString("created_at");
        if (createdStr != null) {
            product.setCreatedAt(DateUtil.fromDbString(createdStr));
        }
        
        String updatedStr = rs.getString("updated_at");
        if (updatedStr != null) {
            product.setUpdatedAt(DateUtil.fromDbString(updatedStr));
        }
        return product;
    }
}


