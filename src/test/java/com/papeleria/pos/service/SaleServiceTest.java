package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;
import com.papeleria.pos.dao.ProductDao;
import com.papeleria.pos.model.CashSession;
import com.papeleria.pos.model.Product;
import com.papeleria.pos.model.Sale;
import com.papeleria.pos.model.SaleItem;
import com.papeleria.pos.util.DateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SaleServiceTest {

    private SaleService saleService;
    private CashService cashService;
    private ProductService productService;

    @BeforeAll
    static void initTestEnvironment() throws Exception {
        Path tempDb = Files.createTempFile("pos_test_", ".db");
        tempDb.toFile().deleteOnExit();
        System.setProperty("pos.db.path", tempDb.toString());
        DatabaseManager.getInstance().initialize();
    }

    @BeforeEach
    void setUp() throws Exception {
        saleService = new SaleService();
        cashService = new CashService();
        productService = new ProductService();
        
        // Clean up data before test
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.createStatement().execute("DELETE FROM cash_closure_corrections");
            conn.createStatement().execute("DELETE FROM cash_closures");
            conn.createStatement().execute("DELETE FROM return_items");
            conn.createStatement().execute("DELETE FROM returns");
            conn.createStatement().execute("DELETE FROM cash_movements");
            conn.createStatement().execute("DELETE FROM inventory_movements");
            conn.createStatement().execute("DELETE FROM sale_items");
            conn.createStatement().execute("DELETE FROM sales");
            conn.createStatement().execute("DELETE FROM cash_sessions");
            conn.createStatement().execute("DELETE FROM products");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up after
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.createStatement().execute("DELETE FROM cash_closure_corrections");
            conn.createStatement().execute("DELETE FROM cash_closures");
            conn.createStatement().execute("DELETE FROM return_items");
            conn.createStatement().execute("DELETE FROM returns");
            conn.createStatement().execute("DELETE FROM cash_movements");
            conn.createStatement().execute("DELETE FROM inventory_movements");
            conn.createStatement().execute("DELETE FROM sale_items");
            conn.createStatement().execute("DELETE FROM sales");
            conn.createStatement().execute("DELETE FROM cash_sessions");
            conn.createStatement().execute("DELETE FROM products");
        }
    }

    @Test
    void testSaleFailsWhenInsufficientStock() {
        // Create product
        Product p = new Product();
        p.setBarcode("12345");
        p.setName("Pen");
        p.setPurchasePrice(new BigDecimal("10.00"));
        p.setSalePrice(new BigDecimal("15.00"));
        p.setStock(5);
        p.setMinStock(1);
        productService.createProduct(p);
        
        // Open session
        cashService.openSession(new BigDecimal("100.00"));
        CashSession session = cashService.getActiveSession();
        
        // Attempt sale with quantity 10
        Sale sale = new Sale();
        sale.setSaleNumber("SALE-001");
        sale.setIdempotencyToken(UUID.randomUUID().toString());
        sale.setCashSessionId(session.getId());
        sale.setPaymentMethod("EFECTIVO");
        sale.setStatus("COMPLETED");
        
        SaleItem item = new SaleItem();
        item.setProductId(p.getId());
        item.setQuantity(10);
        item.setProductNameSnapshot("Pen");
        item.setUnitPriceSnapshot(new BigDecimal("15.00"));
        item.setDiscount(BigDecimal.ZERO);
        item.setSubtotal(new BigDecimal("150.00"));
        
        sale.setItems(List.of(item));
        sale.setSubtotal(new BigDecimal("150.00"));
        sale.setDiscount(BigDecimal.ZERO);
        sale.setAmountPaid(new BigDecimal("150.00"));
        sale.setTotal(new BigDecimal("150.00"));
        
        Exception ex = assertThrows(RuntimeException.class, () -> {
            saleService.completeSale(sale);
        });
        
        assertTrue(ex.getMessage().contains("Insufficient stock"));
        
        // Check stock remained 5 (rollback successful)
        Product updatedP = productService.getByBarcode("12345");
        assertEquals(5, updatedP.getStock());
    }

    @Test
    void testSuccessfulSaleReducesStockAndCreatesMovements() {
        Product p = new Product();
        p.setBarcode("98765");
        p.setName("Notebook");
        p.setPurchasePrice(new BigDecimal("20.00"));
        p.setSalePrice(new BigDecimal("30.00"));
        p.setStock(10);
        p.setMinStock(2);
        productService.createProduct(p);

        cashService.openSession(new BigDecimal("100.00"));
        CashSession session = cashService.getActiveSession();

        Sale sale = new Sale();
        sale.setSaleNumber("SALE-002");
        sale.setIdempotencyToken(UUID.randomUUID().toString());
        sale.setCashSessionId(session.getId());
        sale.setPaymentMethod("EFECTIVO");
        sale.setStatus("COMPLETED");

        SaleItem item = new SaleItem();
        item.setProductId(p.getId());
        item.setQuantity(3);
        item.setProductNameSnapshot("Notebook");
        item.setUnitPriceSnapshot(new BigDecimal("30.00"));
        item.setDiscount(BigDecimal.ZERO);
        item.setSubtotal(new BigDecimal("90.00"));

        sale.setItems(List.of(item));
        sale.setSubtotal(new BigDecimal("90.00"));
        sale.setDiscount(BigDecimal.ZERO);
        sale.setAmountPaid(new BigDecimal("90.00"));
        sale.setTotal(new BigDecimal("90.00"));
        
        saleService.completeSale(sale);

        // Check stock
        Product updatedP = productService.getByBarcode("98765");
        assertEquals(7, updatedP.getStock());
        
        // Expected cash should increase by 90
        BigDecimal expectedCash = cashService.getExpectedCash();
        assertEquals(0, new BigDecimal("190.00").compareTo(expectedCash));
    }

    @org.junit.jupiter.api.AfterAll
    static void cleanupTestEnvironment() {
        System.clearProperty("pos.db.path");
    }
}
