package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens JDBC connections and ensures the required tables exist. */
public final class DatabaseManager {

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL JDBC driver not found on classpath.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DBConfig.getUrl(), DBConfig.getUser(), DBConfig.getPassword());
    }

    /** Creates the schema if it doesn't already exist. Safe to call on every startup. */
    public static void initSchema() {
        String[] ddl = {
            """
            CREATE TABLE IF NOT EXISTS equipment (
                equipment_id     VARCHAR(20) PRIMARY KEY,
                name             VARCHAR(150) NOT NULL,
                category         VARCHAR(50)  NOT NULL,
                daily_rate       NUMERIC(10,2) NOT NULL,
                available        BOOLEAN NOT NULL DEFAULT TRUE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS app_user (
                user_id      VARCHAR(20) PRIMARY KEY,
                full_name    VARCHAR(150) NOT NULL,
                role         VARCHAR(20)  NOT NULL,       -- 'STUDENT' or 'STAFF'
                final_year   BOOLEAN,                     -- used when role = STUDENT
                department   VARCHAR(100)                 -- used when role = STAFF
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS rental (
                rental_id        VARCHAR(20) PRIMARY KEY,
                equipment_id     VARCHAR(20) NOT NULL REFERENCES equipment(equipment_id),
                user_id          VARCHAR(20) NOT NULL REFERENCES app_user(user_id),
                rent_date        DATE NOT NULL,
                due_date         DATE NOT NULL,
                return_date      DATE,
                damage_level     VARCHAR(10) NOT NULL DEFAULT 'NONE', -- NONE / SMALL / MEDIUM / LARGE
                pricing_strategy VARCHAR(60)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS bill (
                rental_id      VARCHAR(20) PRIMARY KEY REFERENCES rental(rental_id),
                base_fee       NUMERIC(10,2) NOT NULL,
                discount       NUMERIC(10,2) NOT NULL,
                penalty        NUMERIC(10,2) NOT NULL,
                strategy_used  VARCHAR(60)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS id_sequence (
                prefix       VARCHAR(5) PRIMARY KEY,   -- 'S' for Student, 'T' for Staff
                next_value   INT NOT NULL              -- next number to hand out; never decreases
            )
            """
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
            migrateDamageColumn(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema: " + e.getMessage(), e);
        }
    }

    /**
     * One-time migration for databases created before the damage-level feature:
     * adds the damage_level column if missing, backfills it from the old
     * "damaged" boolean column (if present), then drops that old column.
     */
    private static void migrateDamageColumn(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE rental ADD COLUMN IF NOT EXISTS damage_level VARCHAR(10) NOT NULL DEFAULT 'NONE'");
        }
        boolean hasOldColumn;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT column_name FROM information_schema.columns " +
                     "WHERE table_name = 'rental' AND column_name = 'damaged'")) {
            hasOldColumn = rs.next();
        }
        if (hasOldColumn) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("UPDATE rental SET damage_level = 'MEDIUM' WHERE damaged = TRUE AND damage_level = 'NONE'");
                stmt.execute("ALTER TABLE rental DROP COLUMN damaged");
            }
        }
    }

    private DatabaseManager() {}
}
