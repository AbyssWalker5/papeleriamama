package com.papeleria.pos.ui.controller;

import com.papeleria.pos.model.CashMovement;
import com.papeleria.pos.service.CashService;
import com.papeleria.pos.ui.component.AlertHelper;
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
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for the Cash module view.
 */
public class CashController {
    private static final Logger logger = LoggerFactory.getLogger(CashController.class);

    @FXML private Label statusLabel;
    @FXML private Button btnOpenCash;
    @FXML private Button btnCashIn;
    @FXML private Button btnCashOut;
    @FXML private Button btnCloseCash;

    @FXML private TableView<CashMovement> movementsTable;
    @FXML private TableColumn<CashMovement, String> colDate;
    @FXML private TableColumn<CashMovement, String> colType;
    @FXML private TableColumn<CashMovement, BigDecimal> colAmount;
    @FXML private TableColumn<CashMovement, String> colDescription;

    private final CashService cashService;
    private final ObservableList<CashMovement> movements = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CashController() {
        this.cashService = new CashService();
    }

    @FXML
    public void initialize() {
        setupTable();
        refreshStatus();
    }

    private void setupTable() {
        movementsTable.setItems(movements);
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt().format(DATE_FORMATTER)));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colAmount.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAmount()));
        colDescription.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getReason()));
    }

    private void refreshStatus() {
        try {
            boolean isOpen = cashService.isSessionOpen();
            if (isOpen) {
                statusLabel.setText("ABIERTA");
                statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                btnOpenCash.setDisable(true);
                btnCashIn.setDisable(false);
                btnCashOut.setDisable(false);
                btnCloseCash.setDisable(false);
                movements.setAll(cashService.getCurrentSessionMovements());
            } else {
                statusLabel.setText("CERRADA");
                statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                btnOpenCash.setDisable(false);
                btnCashIn.setDisable(true);
                btnCashOut.setDisable(true);
                btnCloseCash.setDisable(true);
                movements.clear();
            }
        } catch (Exception e) {
            logger.error("Error checking cash session status", e);
            AlertHelper.showError("Error", "No se pudo obtener el estado de la caja.");
        }
    }

    @FXML
    private void onOpenCash(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Abrir Caja");
        dialog.setHeaderText("Apertura de Caja");
        dialog.setContentText("Fondo inicial:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                BigDecimal initialFund = new BigDecimal(amountStr);
                cashService.openSession(initialFund);
                refreshStatus();
            } catch (NumberFormatException e) {
                AlertHelper.showError("Monto inválido", "Ingrese un monto válido.");
            } catch (Exception e) {
                logger.error("Error opening cash session", e);
                AlertHelper.showError("Error", "No se pudo abrir la caja.");
            }
        });
    }

    @FXML
    private void onCashIn(ActionEvent event) {
        registerMovement("Entrada de Efectivo", true);
    }

    @FXML
    private void onCashOut(ActionEvent event) {
        registerMovement("Salida de Efectivo", false);
    }
    
    private void registerMovement(String title, boolean isIncome) {
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle(title);
        dialog.setHeaderText(title);
        dialog.setContentText("Monto:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                
                TextInputDialog descDialog = new TextInputDialog("");
                descDialog.setTitle("Descripción");
                descDialog.setHeaderText("Motivo del movimiento");
                descDialog.setContentText("Descripción:");
                
                Optional<String> descResult = descDialog.showAndWait();
                String description = descResult.orElse("");

                if (isIncome) {
                    cashService.registerIncome(amount, description);
                } else {
                    cashService.registerExpense(amount, description);
                }
                refreshStatus();
            } catch (NumberFormatException e) {
                AlertHelper.showError("Monto inválido", "Ingrese un monto válido.");
            } catch (Exception e) {
                logger.error("Error registering movement", e);
                AlertHelper.showError("Error", "No se pudo registrar el movimiento.");
            }
        });
    }

    @FXML
    private void onCloseCash(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Corte de Caja");
        confirm.setHeaderText("¿Está seguro de realizar el corte de caja?");
        confirm.setContentText("Esta acción cerrará la sesión actual.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                cashService.closeSession();
                AlertHelper.showInfo("Corte de Caja", "Se ha realizado el corte de caja exitosamente.");
                refreshStatus();
            } catch (Exception e) {
                logger.error("Error closing cash session", e);
                AlertHelper.showError("Error", "No se pudo realizar el corte de caja.");
            }
        }
    }
}
