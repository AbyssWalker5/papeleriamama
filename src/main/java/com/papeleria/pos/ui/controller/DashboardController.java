package com.papeleria.pos.ui.controller;

import com.papeleria.pos.model.Sale;
import com.papeleria.pos.service.ProductService;
import com.papeleria.pos.service.SaleService;
import com.papeleria.pos.util.DateUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @FXML
    private Label lblVentasHoy;

    @FXML
    private Label lblTransacciones;

    @FXML
    private Label lblBajoInventario;

    private final SaleService saleService = new SaleService();
    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        logger.info("Initializing DashboardController");
        loadMetrics();
    }

    @FXML
    void handleRefresh(ActionEvent event) {
        logger.info("Manual refresh requested");
        loadMetrics();
    }

    private void loadMetrics() {
        try {
            LocalDateTime start = DateUtil.startOfDay(LocalDateTime.now());
            LocalDateTime end = DateUtil.endOfDay(LocalDateTime.now());
            List<Sale> salesToday = saleService.getSalesByDateRange(start, end);

            BigDecimal ventasHoy = BigDecimal.ZERO;
            for (Sale s : salesToday) {
                if ("COMPLETED".equalsIgnoreCase(s.getStatus())) {
                    ventasHoy = ventasHoy.add(s.getTotal());
                }
            }
            int transacciones = salesToday.size();
            int bajoInventario = productService.getLowStockAlerts().size();

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            
            lblVentasHoy.setText(currencyFormat.format(ventasHoy));
            lblTransacciones.setText(String.valueOf(transacciones));
            lblBajoInventario.setText(bajoInventario + " productos");
        } catch (Exception e) {
            logger.error("Error loading dashboard metrics", e);
        }
    }
}
