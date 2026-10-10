package com.papeleria.pos.ui.controller;

import com.papeleria.pos.service.BackupService;
import com.papeleria.pos.service.SettingsService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

public class SettingsController {
    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    @FXML private TextField storeNameField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField thankYouMessageField;
    @FXML private TextField ticketWidthField;

    private SettingsService settingsService;
    private BackupService backupService;

    public SettingsController() {
        this.settingsService = new SettingsService();
        this.backupService = new BackupService();
    }

    @FXML
    public void initialize() {
        logger.info("Initializing SettingsController");
        loadSettings();
    }

    private void loadSettings() {
        if (settingsService != null) {
            storeNameField.setText(settingsService.getSetting("store_name"));
            addressField.setText(settingsService.getSetting("store_address"));
            phoneField.setText(settingsService.getSetting("store_phone"));
            thankYouMessageField.setText(settingsService.getSetting("thank_you_message"));
            ticketWidthField.setText(settingsService.getSetting("ticket_width_mm"));
        }
    }

    @FXML
    private void handleSave() {
        if (settingsService != null) {
            settingsService.updateSetting("store_name", storeNameField.getText());
            settingsService.updateSetting("store_address", addressField.getText());
            settingsService.updateSetting("store_phone", phoneField.getText());
            settingsService.updateSetting("thank_you_message", thankYouMessageField.getText());
            
            try {
                int width = Integer.parseInt(ticketWidthField.getText());
                settingsService.updateSetting("ticket_width_mm", String.valueOf(width));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Ancho de ticket debe ser un número válido.");
                return;
            }

            logger.info("Settings saved");
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Configuración guardada correctamente.");
        }
    }

    @FXML
    private void handleCreateBackup() {
        if (backupService != null) {
            logger.info("Creating backup...");
            try {
                backupService.createBackup();
                showAlert(Alert.AlertType.INFORMATION, "Respaldo", "Respaldo creado exitosamente.");
            } catch (Exception e) {
                logger.error("Error al crear el respaldo", e);
                showAlert(Alert.AlertType.ERROR, "Respaldo", "Error al crear el respaldo.");
            }
        }
    }

    @FXML
    private void handleRestoreBackup() {
        if (backupService != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Restaurar Respaldo");
            dialog.setHeaderText("Ruta del archivo de respaldo");
            dialog.setContentText("Ruta:");
            Optional<String> result = dialog.showAndWait();
            
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                logger.info("Restoring backup...");
                try {
                    backupService.restoreBackup(Path.of(result.get().trim()));
                    showAlert(Alert.AlertType.INFORMATION, "Restauración", "Respaldo restaurado exitosamente. Reinicie la aplicación.");
                } catch (Exception e) {
                    logger.error("Error al restaurar el respaldo", e);
                    showAlert(Alert.AlertType.ERROR, "Restauración", "Error al restaurar el respaldo.");
                }
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
