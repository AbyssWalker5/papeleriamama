package com.papeleria.pos.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Load dashboard by default
        loadView("dashboard.fxml");
    }

    @FXML
    void handleDashboard(ActionEvent event) {
        loadView("dashboard.fxml");
    }

    @FXML
    void handlePOS(ActionEvent event) {
        loadView("pos.fxml");
    }

    @FXML
    void handleInventario(ActionEvent event) {
        loadView("inventory.fxml");
    }

    @FXML
    void handleHistorial(ActionEvent event) {
        loadView("sales_history.fxml");
    }

    @FXML
    void handleCaja(ActionEvent event) {
        loadView("cash.fxml");
    }

    @FXML
    void handleReportes(ActionEvent event) {
        loadView("reports.fxml");
    }

    @FXML
    void handleConfig(ActionEvent event) {
        loadView("settings.fxml");
    }

    private void loadView(String viewName) {
        try {
            URL resource = getClass().getResource("/com/papeleria/pos/views/" + viewName);
            if (resource == null) {
                logger.warn("View not found: {}", viewName);
                return;
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();
            contentArea.getChildren().setAll(view);
            logger.info("Loaded view: {}", viewName);
        } catch (IOException e) {
            logger.error("Failed to load view: {}", viewName, e);
        }
    }
}
