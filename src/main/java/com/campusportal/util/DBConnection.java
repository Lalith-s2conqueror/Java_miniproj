package com.campusportal.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connectivity for the application.
 *
 * <p>Loads configuration from {@code db.properties} on the classpath.
 * If {@code db.properties} is not present, falls back gracefully to template or default values.
 */
public final class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());
    private static final Properties PROPERTIES = new Properties();

    private static String url;
    private static String username;
    private static String password;
    private static String driver;

    /** Functional interface allowing test suites to inject custom connection providers. */
    @FunctionalInterface
    public interface ConnectionProvider {
        Connection getConnection() throws SQLException;
    }

    private static ConnectionProvider customProvider = null;

    static {
        loadConfiguration();
    }

    private DBConnection() {
        // Utility class; prevent instantiation
    }

    private static void loadConfiguration() {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPERTIES.load(in);
            } else {
                // Try db.properties.template as fallback
                try (InputStream templateIn = DBConnection.class.getClassLoader().getResourceAsStream("db.properties.template")) {
                    if (templateIn != null) {
                        PROPERTIES.load(templateIn);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load database properties file", e);
        }

        url = PROPERTIES.getProperty("db.url",
                "jdbc:mysql://localhost:3306/campus_complaint_portal?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        username = PROPERTIES.getProperty("db.username", "root");
        password = PROPERTIES.getProperty("db.password", "");
        driver = PROPERTIES.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "JDBC Driver class not found: " + driver, e);
        }
    }

    /**
     * Obtains a new database connection.
     *
     * @return an active {@link Connection}
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (customProvider != null) {
            return customProvider.getConnection();
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Sets a custom connection provider (primarily for unit and integration testing).
     *
     * @param provider the provider to use, or {@code null} to reset to default
     */
    public static void setConnectionProvider(ConnectionProvider provider) {
        customProvider = provider;
    }

    /**
     * Safely closes one or more closeable resources (Connection, Statement, ResultSet, etc.).
     *
     * @param closeables resources to close; nulls are ignored
     */
    public static void close(AutoCloseable... closeables) {
        if (closeables == null) {
            return;
        }
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Error closing resource", e);
                }
            }
        }
    }
}
