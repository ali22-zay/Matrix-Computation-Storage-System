package matrix.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages the JDBC connection to MySQL and ensures the required schema exists.
 *
 * Configuration: edit the constants below to match your MySQL installation.
 */
public class DatabaseManager {

    // ─── Configuration ────────────────────────────────────────────────────────
    //  Change these to match your MySQL setup
    private static final String DB_HOST     = "localhost";
    private static final String DB_PORT     = "3306";
    private static final String DB_NAME     = "matrix_db";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "Everyday_Learn";          // ← your MySQL password

    private static final String JDBC_URL =
        "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        + "&createDatabaseIfNotExist=true";

    // ─── DDL ──────────────────────────────────────────────────────────────────
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS matrix_operations (
            id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
            operation_type VARCHAR(30)  NOT NULL,
            matrix_a       TEXT         NOT NULL,
            matrix_b       TEXT,
            result_matrix  TEXT         NOT NULL,
            scalar_result  DOUBLE,
            rows_a         INT          NOT NULL,
            cols_a         INT          NOT NULL,
            created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

    // ─── Singleton-style connection pool (single connection) ──────────────────
    private static Connection connection = null;

    /**
     * Returns a live connection, creating one if necessary.
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "MySQL JDBC Driver not found.\n"
                    + "Add mysql-connector-j-*.jar to your classpath.", e);
            }
            connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
            ensureSchema(connection);
        }
        return connection;
    }

    /**
     * Creates the database schema if it does not yet exist.
     */
    private static void ensureSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(CREATE_TABLE_SQL);
        }
    }

    /** Closes the shared connection (call on application exit). */
    public static void close() {
        if (connection != null) {
            try { connection.close(); }
            catch (SQLException ignored) {}
            connection = null;
        }
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    /** Returns a human-readable connection summary for UI status bars. */
    public static String getConnectionInfo() {
        return DB_USER + "@" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
    }
}
