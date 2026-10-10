package com.papeleria.pos.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Cross-platform application configuration.
 */
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    public static final String APP_NAME = "Papelería POS";
    public static final String APP_VERSION = "1.0.0";
    public static final String DB_FILENAME = "papeleria.db";

    /**
     * Gets the main data directory for the application.
     * @return Path to the data directory.
     */
    public static Path getDataDirectory() {
        String userHome = System.getProperty("user.home");
        Path dataDir = Paths.get(userHome, ".papeleria-pos");
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
                logger.debug("Created data directory at {}", dataDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create data directory at {}", dataDir, e);
            throw new RuntimeException("Could not create application data directory", e);
        }
        return dataDir;
    }

    /**
     * Gets the path to the database file.
     * @return Path to the SQLite database file.
     */
    public static Path getDatabasePath() {
        String customPath = System.getProperty("pos.db.path");
        if (customPath != null && !customPath.trim().isEmpty()) {
            return Paths.get(customPath);
        }
        return getDataDirectory().resolve(DB_FILENAME);
    }

    /**
     * Gets the backup directory.
     * @return Path to the backup directory.
     */
    public static Path getBackupDirectory() {
        Path backupDir = getDataDirectory().resolve("backups/");
        try {
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
                logger.debug("Created backup directory at {}", backupDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create backup directory at {}", backupDir, e);
        }
        return backupDir;
    }

    /**
     * Gets the tickets directory.
     * @return Path to the tickets directory.
     */
    public static Path getTicketsDirectory() {
        Path ticketsDir = getDataDirectory().resolve("tickets/");
        try {
            if (!Files.exists(ticketsDir)) {
                Files.createDirectories(ticketsDir);
                logger.debug("Created tickets directory at {}", ticketsDir);
            }
        } catch (IOException e) {
            logger.error("Failed to create tickets directory at {}", ticketsDir, e);
        }
        return ticketsDir;
    }

    /**
     * Gets the JDBC URL for the SQLite database.
     * @return JDBC URL string.
     */
    public static String getJdbcUrl() {
        return "jdbc:sqlite:" + getDatabasePath().toString();
    }
}
