package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;
import com.papeleria.pos.dao.ProductDao;
import com.papeleria.pos.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Service class handling business logic for Products.
 */
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final ProductDao productDao;

    public ProductService() {
        this.productDao = new ProductDao();
    }

    public List<Product> getAll() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return productDao.getAll(conn);
        } catch (SQLException e) {
            logger.error("Error retrieving all products", e);
            throw new RuntimeException("Database error retrieving products", e);
        }
    }

    public Product getByBarcode(String barcode) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return productDao.getByBarcode(conn, barcode);
        } catch (SQLException e) {
            logger.error("Error retrieving product with barcode: {}", barcode, e);
            throw new RuntimeException("Database error retrieving product", e);
        }
    }

    public void createProduct(Product product) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            // Validate barcode uniqueness
            if (product.getBarcode() != null && !product.getBarcode().isEmpty()) {
                Product existing = productDao.getByBarcode(conn, product.getBarcode());
                if (existing != null) {
                    throw new RuntimeException("Product with barcode " + product.getBarcode() + " already exists");
                }
            }
            productDao.insert(conn, product);
            logger.info("Created new product with barcode: {}", product.getBarcode());
        } catch (SQLException e) {
            logger.error("Error creating product: {}", product.getBarcode(), e);
            throw new RuntimeException("Database error creating product", e);
        }
    }

    public void updateProduct(Product product) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            productDao.update(conn, product);
            logger.info("Updated product ID: {}", product.getId());
        } catch (SQLException e) {
            logger.error("Error updating product ID: {}", product.getId(), e);
            throw new RuntimeException("Database error updating product", e);
        }
    }

    public void deactivateProduct(int productId) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            Product product = productDao.getById(conn, productId);
            if (product != null) {
                product.setActive(false);
                productDao.update(conn, product);
            }
            logger.info("Deactivated product ID: {}", productId);
        } catch (SQLException e) {
            logger.error("Error deactivating product ID: {}", productId, e);
            throw new RuntimeException("Database error deactivating product", e);
        }
    }

    public List<Product> search(String term) {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return productDao.search(conn, term);
        } catch (SQLException e) {
            logger.error("Error searching products with term: {}", term, e);
            throw new RuntimeException("Database error searching products", e);
        }
    }

    public List<Product> getLowStockAlerts() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            return productDao.getLowStock(conn);
        } catch (SQLException e) {
            logger.error("Error retrieving low stock alerts", e);
            throw new RuntimeException("Database error retrieving low stock alerts", e);
        }
    }
}
