package com.papeleria.pos.service;

import com.papeleria.pos.dao.DatabaseManager;
import com.papeleria.pos.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    public BackupService() {
    }

    public void createBackup() {
        logger.info("Starting database backup process...");
        try {
            Path backupDirectory = AppConfig.getBackupDirectory();
            if (!Files.exists(backupDirectory)) {
                Files.createDirectories(backupDirectory);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "backup_" + timestamp + ".db";
            Path backupFilePath = backupDirectory.resolve(backupFileName);

            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 Statement stmt = conn.createStatement()) {
                 
                // Verify integrity before backup
                try (ResultSet rs = stmt.executeQuery("PRAGMA integrity_check;")) {
                    if (rs.next()) {
                        String result = rs.getString(1);
                        if (!"ok".equalsIgnoreCase(result)) {
                            logger.error("Database integrity check failed: {}", result);
                            throw new IllegalStateException("Cannot backup: Database integrity check failed.");
                        }
                    }
                }

                String sql = String.format("VACUUM INTO '%s'", backupFilePath.toAbsolutePath().toString().replace("\\", "/"));
                stmt.execute(sql);
                logger.info("Database backup created successfully at: {}", backupFilePath);
            }
        } catch (SQLException | IOException e) {
            logger.error("Failed to create database backup", e);
            throw new RuntimeException("Failed to create backup", e);
        }
    }

    public void restoreBackup(Path backupFile) {
        logger.info("Starting database restoration from: {}", backupFile);
        
        if (!Files.exists(backupFile)) {
            throw new IllegalArgumentException("Backup file does not exist: " + backupFile);
        }

        try {
            Path databasePath = AppConfig.getDatabasePath();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path safetyBackupPath = databasePath.getParent().resolve("safety_backup_" + timestamp + ".db");
            
            if (Files.exists(databasePath)) {
                Files.copy(databasePath, safetyBackupPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("Created safety backup of current database at: {}", safetyBackupPath);
            }

            Files.copy(backupFile, databasePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Database restored successfully from: {}", backupFile);
            
        } catch (IOException e) {
            logger.error("Failed to restore database from backup", e);
            throw new RuntimeException("Failed to restore database", e);
        }
    }
}
