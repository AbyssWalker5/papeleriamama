package com.papeleria.pos.ui.controller;

import com.papeleria.pos.model.Sale;
import com.papeleria.pos.model.SaleItem;
import com.papeleria.pos.model.SaleReturn;
import com.papeleria.pos.model.ReturnItem;
import com.papeleria.pos.service.ReturnService;
import com.papeleria.pos.service.SaleService;
import com.papeleria.pos.ui.component.AlertHelper;
import com.papeleria.pos.util.DateUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SalesHistoryController {
    private static final Logger logger = LoggerFactory.getLogger(SalesHistoryController.class);

    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, String> colSaleNumber;
    @FXML private TableColumn<Sale, String> colDate;
    @FXML private TableColumn<Sale, String> colPayment;
    @FXML private TableColumn<Sale, BigDecimal> colTotal;
    @FXML private TableColumn<Sale, String> colStatus;

    @FXML private TableView<SaleItem> itemsTable;
    @FXML private TableColumn<SaleItem, String> colItemProduct;
    @FXML private TableColumn<SaleItem, Integer> colItemQty;
    @FXML private TableColumn<SaleItem, BigDecimal> colItemPrice;
    @FXML private TableColumn<SaleItem, BigDecimal> colItemSubtotal;

    private final SaleService saleService = new SaleService();
    private final ReturnService returnService = new ReturnService();

    private final ObservableList<Sale> salesList = FXCollections.observableArrayList();
    private final ObservableList<SaleItem> itemsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colSaleNumber.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSaleNumber()));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt() != null ? DateUtil.formatDisplay(data.getValue().getCreatedAt()) : ""));
        colPayment.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPaymentMethod()));
        colTotal.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTotal()));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        colItemProduct.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductNameSnapshot()));
        colItemQty.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getQuantity()));
        colItemPrice.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getUnitPriceSnapshot()));
        colItemSubtotal.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSubtotal()));

        salesTable.setItems(salesList);
        itemsTable.setItems(itemsList);

        salesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getItems() != null) {
                itemsList.setAll(newVal.getItems());
            } else {
                itemsList.clear();
            }
        });

        loadSales();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadSales();
    }

    private void loadSales() {
        try {
            LocalDateTime start = LocalDateTime.now().minusDays(30);
            LocalDateTime end = LocalDateTime.now().plusDays(1);
            List<Sale> sales = saleService.getSalesByDateRange(start, end);
            salesList.setAll(sales);
            if (!sales.isEmpty()) {
                salesTable.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            logger.error("Error loading sales history", e);
            AlertHelper.showError("Error", "No se pudo cargar el historial de ventas.");
        }
    }

    @FXML
    private void handleReturn(ActionEvent event) {
        Sale selected = salesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning("Selección requerida", "Seleccione una venta para procesar devolución.");
            return;
        }

        if (!"COMPLETED".equalsIgnoreCase(selected.getStatus())) {
            AlertHelper.showWarning("No permitida", "Solo se pueden hacer devoluciones sobre ventas completadas.");
            return;
        }

        TextInputDialog reasonDialog = new TextInputDialog("Devolución por cliente");
        reasonDialog.setTitle("Devolución");
        reasonDialog.setHeaderText("Devolución para venta " + selected.getSaleNumber());
        reasonDialog.setContentText("Motivo:");

        Optional<String> reasonOpt = reasonDialog.showAndWait();
        if (reasonOpt.isEmpty()) return;

        try {
            SaleReturn ret = new SaleReturn();
            ret.setSaleId(selected.getId());
            ret.setReturnNumber("DEV-" + System.currentTimeMillis());
            ret.setTotal(selected.getTotal());
            ret.setReason(reasonOpt.get().trim());
            ret.setPaymentMethodRefund(selected.getPaymentMethod());
            ret.setStatus("COMPLETED");

            List<ReturnItem> retItems = new ArrayList<>();
            if (selected.getItems() != null) {
                for (SaleItem si : selected.getItems()) {
                    ReturnItem ri = new ReturnItem();
                    ri.setSaleItemId(si.getId());
                    ri.setProductId(si.getProductId());
                    ri.setQuantity(si.getQuantity());
                    ri.setUnitPrice(si.getUnitPriceSnapshot());
                    ri.setSubtotal(si.getSubtotal());
                    retItems.add(ri);
                }
            }
            ret.setItems(retItems);

            returnService.processReturn(ret);
            AlertHelper.showInfo("Éxito", "Devolución registrada correctamente.");
            loadSales();
        } catch (Exception e) {
            logger.error("Error processing return", e);
            AlertHelper.showError("Error", "No se pudo procesar la devolución: " + e.getMessage());
        }
    }
}

