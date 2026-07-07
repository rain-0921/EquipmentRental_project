package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton wrapper around {@link DriverManager}. Caches one
 * connection per call (DriverManager already pools internally on
 * modern MySQL drivers) and offers helpers for try-with-resources
 * via {@link #close(AutoCloseable...)}.
 */
public class DatabaseManager {

    private static final DatabaseManager INSTANCE = new DatabaseManager();
    private final DbConfig config;

    private DatabaseManager() {
        this.config = new DbConfig();
    }

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    public DbConfig config() {
        return config;
    }

    /**
     * Open a fresh JDBC connection. Caller is responsible for
     * closing it (use try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        String url = buildUrl();
        return DriverManager.getConnection(
            url,
            config.user(),
            config.password());
    }

    /** Force a connection so callers can fail fast at startup. */
    public boolean testConnection() {
        try (Connection c = getConnection()) {
            return c.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    private String buildUrl() {
        return String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=%b&allowPublicKeyRetrieval=%b&serverTimezone=UTC",
            config.host(),
            config.port(),
            config.database(),
            config.useSSL(),
            config.allowPublicKeyRetrieval());
    }

    /** Tiny helper to close multiple JDBC resources without
     *  littering the code with try/catch blocks. */
    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try { c.close(); } catch (Exception ignored) { /* best effort */ }
            }
        }
    }
}