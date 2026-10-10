package com.papeleria.pos.ui.controller;

import com.papeleria.pos.model.Product;
import com.papeleria.pos.model.Sale;
import com.papeleria.pos.service.ProductService;
import com.papeleria.pos.service.SaleService;
import com.papeleria.pos.ui.component.AlertHelper;
import com.papeleria.pos.util.CsvExporter;
import com.papeleria.pos.util.DateUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class ReportsController {
    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    @FXML private Label lblTotalInventoryValue;
    @FXML private Label lblTotalProductsCount;
    @FXML private Label lblMonthlySales;

    private final ProductService productService = new ProductService();
    private final SaleService saleService = new SaleService();

    @FXML
    public void initialize() {
        loadReportSummary();
    }

    private void loadReportSummary() {
        try {
            List<Product> products = productService.getAll();
            BigDecimal totalValue = BigDecimal.ZERO;
            for (Product p : products) {
                if (p.getSalePrice() != null) {
                    totalValue = totalValue.add(p.getSalePrice().multiply(BigDecimal.valueOf(p.getStock())));
                }
            }

            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = LocalDateTime.now();
            List<Sale> salesMonth = saleService.getSalesByDateRange(startOfMonth, endOfMonth);

            BigDecimal totalSalesMonth = BigDecimal.ZERO;
            for (Sale s : salesMonth) {
                if ("COMPLETED".equalsIgnoreCase(s.getStatus())) {
                    totalSalesMonth = totalSalesMonth.add(s.getTotal());
                }
            }

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            lblTotalInventoryValue.setText(currencyFormat.format(totalValue));
            lblTotalProductsCount.setText(products.size() + " productos registrados");
            lblMonthlySales.setText(currencyFormat.format(totalSalesMonth));
        } catch (Exception e) {
            logger.error("Error generating report summary", e);
        }
    }

    @FXML
    private void handleExportProductsCsv(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Inventario CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("inventario_" + System.currentTimeMillis() + ".csv");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                List<Product> products = productService.getAll();
                CsvExporter.exportProducts(products, file.toPath());
                AlertHelper.showInfo("Exportación Exitosa", "Inventario exportado correctamente a:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Error exporting products to CSV", e);
                AlertHelper.showError("Error al Exportar", "No se pudo exportar el archivo CSV: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportSalesCsv(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Historial de Ventas CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("ventas_" + System.currentTimeMillis() + ".csv");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                LocalDateTime start = LocalDateTime.now().minusDays(90);
                LocalDateTime end = LocalDateTime.now().plusDays(1);
                List<Sale> sales = saleService.getSalesByDateRange(start, end);
                CsvExporter.exportSales(sales, file.toPath());
                AlertHelper.showInfo("Exportación Exitosa", "Ventas exportadas correctamente a:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                logger.error("Error exporting sales to CSV", e);
                AlertHelper.showError("Error al Exportar", "No se pudo exportar el archivo CSV: " + e.getMessage());
            }
        }
    }
}

