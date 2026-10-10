package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;
import com.papeleria.pos.model.CashSession;
import com.papeleria.pos.model.Product;
import com.papeleria.pos.model.Sale;
import com.papeleria.pos.model.SaleItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;
    private SaleService saleService;
    private CashService cashService;

    @BeforeAll
    static void initTestEnvironment() throws Exception {
        if (System.getProperty("pos.db.path") == null) {
            Path tempDb = Files.createTempFile("pos_test_prod_", ".db");
            tempDb.toFile().deleteOnExit();
            System.setProperty("pos.db.path", tempDb.toString());
            DatabaseManager.getInstance().initialize();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        productService = new ProductService();
        saleService = new SaleService();
        cashService = new CashService();

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
    void testCreateAndEditProduct() {
        Product p = new Product();
        p.setName("Cuaderno Profesional");
        p.setBarcode("750100000001");
        p.setPurchasePrice(new BigDecimal("15.50"));
        p.setSalePrice(new BigDecimal("25.00"));
        p.setStock(20);
        p.setMinStock(5);
        p.setUnit("pieza");
        p.setActive(true);

        productService.createProduct(p);
        assertTrue(p.getId() > 0);

        // Edit product
        p.setName("Cuaderno Profesional Cuadro Grande");
        p.setSalePrice(new BigDecimal("28.00"));
        p.setStock(35);
        productService.updateProduct(p);

        Product updated = productService.getById(p.getId());
        assertNotNull(updated);
        assertEquals("Cuaderno Profesional Cuadro Grande", updated.getName());
        assertEquals(new BigDecimal("28.00"), updated.getSalePrice());
        assertEquals(35, updated.getStock());
    }

    @Test
    void testUpdateRejectsDuplicateBarcode() {
        Product p1 = new Product();
        p1.setName("Lápiz HB");
        p1.setBarcode("750111111111");
        p1.setPurchasePrice(new BigDecimal("3.00"));
        p1.setSalePrice(new BigDecimal("6.00"));
        p1.setStock(50);
        p1.setMinStock(10);
        p1.setUnit("pieza");
        p1.setActive(true);
        productService.createProduct(p1);

        Product p2 = new Product();
        p2.setName("Goma de Borrar");
        p2.setBarcode("750222222222");
        p2.setPurchasePrice(new BigDecimal("4.00"));
        p2.setSalePrice(new BigDecimal("8.00"));
        p2.setStock(30);
        p2.setMinStock(5);
        p2.setUnit("pieza");
        p2.setActive(true);
        productService.createProduct(p2);

        // Attempting to change p2's barcode to p1's barcode should fail
        p2.setBarcode("750111111111");
        assertThrows(RuntimeException.class, () -> productService.updateProduct(p2));
    }

    @Test
    void testDeleteProductWithoutHistoryIsPermanent() {
        Product p = new Product();
        p.setName("Regla 30cm");
        p.setBarcode("750333333333");
        p.setPurchasePrice(new BigDecimal("5.00"));
        p.setSalePrice(new BigDecimal("12.00"));
        p.setStock(0); // 0 stock so no initial movement
        p.setMinStock(2);
        p.setUnit("pieza");
        p.setActive(true);
        productService.createProduct(p);

        int id = p.getId();
        boolean permanentlyDeleted = productService.deleteProduct(id);
        assertTrue(permanentlyDeleted);

        assertNull(productService.getById(id));
    }

    @Test
    void testDeleteProductWithSalesHistoryIsDeactivated() {
        Product p = new Product();
        p.setName("Tijeras Escolares");
        p.setBarcode("750444444444");
        p.setPurchasePrice(new BigDecimal("10.00"));
        p.setSalePrice(new BigDecimal("20.00"));
        p.setStock(10);
        p.setMinStock(2);
        p.setUnit("pieza");
        p.setActive(true);
        productService.createProduct(p);

        // Open cash session and make a sale
        cashService.openSession(new BigDecimal("100.00"));
        CashSession session = cashService.getActiveSession();

        Sale sale = new Sale();
        sale.setSaleNumber("SALE-PROD-DEL");
        sale.setIdempotencyToken(UUID.randomUUID().toString());
        sale.setCashSessionId(session.getId());
        sale.setStatus("COMPLETED");
        sale.setSubtotal(new BigDecimal("20.00"));
        sale.setDiscount(BigDecimal.ZERO);
        sale.setTotal(new BigDecimal("20.00"));
        sale.setPaymentMethod("EFECTIVO");
        sale.setAmountPaid(new BigDecimal("20.00"));
        sale.setChange(BigDecimal.ZERO);

        SaleItem item = new SaleItem();
        item.setProductId(p.getId());
        item.setProductNameSnapshot(p.getName());
        item.setQuantity(1);
        item.setUnitPriceSnapshot(new BigDecimal("20.00"));
        item.setDiscount(BigDecimal.ZERO);
        item.setSubtotal(new BigDecimal("20.00"));
        sale.setItems(List.of(item));

        saleService.completeSale(sale);

        // Deleting the product should safely deactivate instead of violating FK
        boolean permanentlyDeleted = productService.deleteProduct(p.getId());
        assertFalse(permanentlyDeleted, "Product with sales should be deactivated, not physically deleted");

        // Should not appear in active products
        List<Product> activeProducts = productService.getAll();
        assertTrue(activeProducts.stream().noneMatch(prod -> prod.getId() == p.getId()));

        // But still exists in DB as active = false
        Product inDb = productService.getById(p.getId());
        assertNotNull(inDb);
        assertFalse(inDb.isActive());
    }
}
