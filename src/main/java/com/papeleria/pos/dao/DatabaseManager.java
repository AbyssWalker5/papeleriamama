package com.papeleria.pos.dao;

import com.papeleria.pos.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton that manages the SQLite connection.
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static volatile DatabaseManager instance;

    private DatabaseManager() {
        // Private constructor for singleton
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initializes the database on startup.
     */
    public void initialize() {
        logger.info("Initializing database...");
        try (Connection conn = DriverManager.getConnection(AppConfig.getJdbcUrl())) {
            
            try (Statement stmt = conn.createStatement()) {
                boolean pragmaWal = stmt.execute("PRAGMA journal_mode=WAL;");
                logger.debug("PRAGMA journal_mode=WAL result: {}", pragmaWal);

                boolean pragmaFk = stmt.execute("PRAGMA foreign_keys=ON;");
                logger.debug("PRAGMA foreign_keys=ON result: {}", pragmaFk);

                boolean pragmaBusy = stmt.execute("PRAGMA busy_timeout=5000;");
                logger.debug("PRAGMA busy_timeout=5000 result: {}", pragmaBusy);
            }

            MigrationRunner.run(conn);

        } catch (SQLException e) {
            logger.error("Error initializing database", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    /**
     * Gets a new connection to the database.
     * @return A new Connection object.
     * @throws SQLException If a database access error occurs.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(AppConfig.getJdbcUrl());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Gets a new connection with autocommit disabled for transactions.
     * @return A new Connection object with autocommit set to false.
     * @throws SQLException If a database access error occurs.
     */
    public Connection getTransactionConnection() throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    /**
     * Closes the given connection safely.
     * @param conn The connection to close.
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }

    /**
     * Shuts down any resources if needed.
     */
    public void shutdown() {
        logger.info("Shutting down DatabaseManager...");
        // For SQLite, just ensuring no connections leak is generally enough, 
        // as each getConnection is a fresh connection.
    }
}


