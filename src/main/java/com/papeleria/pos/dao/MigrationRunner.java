package com.papeleria.pos.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Runs versioned SQL migrations for the SQLite database.
 * Migrations are forward-only and executed in order.
 * If a migration fails, the application does NOT start.
 */
public class MigrationRunner {
    private static final Logger logger = LoggerFactory.getLogger(MigrationRunner.class);

    /**
     * Static list of migration scripts in order.
     * Each filename must follow the pattern V{n}__description.sql
     */
    private static final List<String> MIGRATION_FILES = List.of(
            "V1__initial_schema.sql",
            "V2__seed_initial_catalog.sql",
            "V3__renumber_products.sql"
    );

    /**
     * Runs all pending migrations on the given connection.
     *
     * @param connection an open SQLite connection (autocommit will be managed internally)
     * @throws SQLException if a migration fails
     */
    public static void run(Connection connection) throws SQLException {
        logger.info("Checking for database migrations...");

        int currentVersion = getCurrentVersion(connection);
        logger.info("Current schema version: {}", currentVersion);

        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            for (String filename : MIGRATION_FILES) {
                int fileVersion = extractVersion(filename);
                if (fileVersion > currentVersion) {
                    applyMigration(connection, filename, fileVersion);
                }
            }
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }

        logger.info("Migrations completed.");
    }

    /**
     * Gets the current schema version from the database.
     * Returns 0 if schema_version table does not exist (fresh database).
     */
    private static int getCurrentVersion(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='schema_version'");
            if (!rs.next()) {
                logger.info("Fresh database detected.");
                return 0;
            }
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(version) FROM schema_version")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Applies a single migration script within a transaction.
     * If the migration fails, it is rolled back and a RuntimeException is thrown.
     */
    private static void applyMigration(Connection connection, String filename, int version) throws SQLException {
        logger.info("Applying migration: {} (version {})", filename, version);

        String sql = loadResource("db/migrations/" + filename);
        if (sql == null || sql.trim().isEmpty()) {
            throw new RuntimeException("Migration script is empty or not found: " + filename);
        }

        connection.setAutoCommit(false);
        try {
            // Remove comments and split by semicolon
            String[] lines = sql.split("\n");
            StringBuilder cleanSql = new StringBuilder();
            for (String line : lines) {
                if (!line.trim().startsWith("--")) {
                    cleanSql.append(line).append("\n");
                }
            }

            String[] statements = cleanSql.toString().split(";");
            try (Statement stmt = connection.createStatement()) {
                for (String singleStmt : statements) {
                    String trimmed = singleStmt.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }

            connection.commit();
            logger.info("Successfully applied migration: {}", filename);
        } catch (Exception e) {
            connection.rollback();
            logger.error("Migration FAILED for script: {}. Rolling back.", filename, e);
            throw new RuntimeException("Migration failed: " + filename + ". Application cannot start.", e);
        }
    }

    /**
     * Extracts the version number from a migration filename.
     * Expected format: V{n}__description.sql
     */
    private static int extractVersion(String filename) {
        if (filename.startsWith("V") && filename.contains("__")) {
            String versionStr = filename.substring(1, filename.indexOf("__"));
            return Integer.parseInt(versionStr);
        }
        throw new IllegalArgumentException("Invalid migration filename format: " + filename);
    }

    /**
     * Loads a classpath resource as a String.
     */
    private static String loadResource(String path) {
        try (InputStream in = MigrationRunner.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                return null;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                return sb.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource: " + path, e);
        }
    }
}


