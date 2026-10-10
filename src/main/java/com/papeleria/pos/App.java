package com.papeleria.pos;

import com.papeleria.pos.config.AppConfig;
import com.papeleria.pos.dao.DatabaseManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    @Override
    public void init() {
        // Initialize database
        logger.info("Initializing Papelería POS v{}", AppConfig.APP_VERSION);
        try {
            DatabaseManager.getInstance().initialize();
            logger.info("Database initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database", e);
            // Will show error in start() method
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/papeleria/pos/views/main.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 750);
            scene.getStylesheets().add(getClass().getResource("/com/papeleria/pos/styles/application.css").toExternalForm());
            primaryStage.setTitle(AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(680);
            primaryStage.setMaximized(true);
            primaryStage.show();
            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            // Show error dialog since UI is available
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo iniciar la aplicación");
            alert.setContentText("Error: " + e.getMessage() + "\nRevise los logs para más detalles.");
            alert.showAndWait();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        logger.info("Shutting down application");
        DatabaseManager.getInstance().shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
