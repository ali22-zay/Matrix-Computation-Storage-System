package matrix.db;

import matrix.core.Matrix;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for matrix_operations table.
 *
 * Provides save() and getHistory() methods used by the GUI.
 */
public class MatrixOperationDAO {

    // ─── Insert ───────────────────────────────────────────────────────────────

    /**
     * Persists a matrix operation record.
     *
     * @param type         operation name  (e.g. "ADD", "DETERMINANT")
     * @param matrixA      first input matrix
     * @param matrixB      second input matrix (nullable for unary ops)
     * @param resultMatrix result matrix (nullable for scalar results)
     * @param scalarResult scalar result (null if result is a matrix)
     * @return the auto-generated row id, or -1 on failure
     */
    public static long save(String type,
                            Matrix matrixA,
                            Matrix matrixB,
                            Matrix resultMatrix,
                            Double scalarResult) {
        final String sql = """
            INSERT INTO matrix_operations
                (operation_type, matrix_a, matrix_b, result_matrix, scalar_result, rows_a, cols_a, rows_b, cols_b)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, type);
            ps.setString(2, matrixA.toCompactString());
            ps.setString(3, matrixB != null ? matrixB.toCompactString() : null);
            ps.setString(4, resultMatrix != null ? resultMatrix.toCompactString() : "N/A");
            if (scalarResult != null) ps.setDouble(5, scalarResult);
            else ps.setNull(5, Types.DOUBLE);
            ps.setInt(6, matrixA.getRows());
            ps.setInt(7, matrixA.getCols());
            if (matrixB != null) {
                ps.setInt(8, matrixB.getRows());
                ps.setInt(9, matrixB.getCols());
            } else {
                ps.setNull(8, Types.INTEGER);
                ps.setNull(9, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Save failed: " + e.getMessage());
        }
        return -1;
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    /**
     * Retrieves the full operation history, most recent first.
     *
     * @param limit maximum number of rows to return (0 = all)
     */
    public static List<OperationRecord> getHistory(int limit) {
        String sql = "SELECT * FROM matrix_operations ORDER BY created_at DESC"
                     + (limit > 0 ? " LIMIT " + limit : "");
        List<OperationRecord> records = new ArrayList<>();
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                records.add(new OperationRecord(
                    rs.getLong("id"),
                    rs.getString("operation_type"),
                    rs.getString("matrix_a"),
                    rs.getString("matrix_b"),
                    rs.getString("result_matrix"),
                    rs.getObject("scalar_result") != null ? rs.getDouble("scalar_result") : null,
                    rs.getInt("rows_a"),
                    rs.getInt("cols_a"),
                    rs.getInt("rows_b"),
                    rs.getInt("cols_b"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Query failed: " + e.getMessage());
        }
        return records;
    }

    /**
     * Returns the total number of saved operations.
     */
    public static int countAll() {
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM matrix_operations")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DAO] Count failed: " + e.getMessage());
        }
        return 0;
    }

    // ─── Inner record class ───────────────────────────────────────────────────

    /**
     * Immutable value object representing one row from matrix_operations.
     */
    public record OperationRecord(
        long     id,
        String   operationType,
        String   matrixA,
        String   matrixB,
        String   resultMatrix,
        Double   scalarResult,
        int      rowsA,
        int      colsA,
        int      rowsB,
        int      colsB,
        Timestamp createdAt
    ) {
        /** Formats the result for table display. */
        public String displayResult() {
            if (scalarResult != null) {
                double v = scalarResult;
                return (v == (long) v) ? String.valueOf((long) v) : String.format("%.6f", v);
            }
            return resultMatrix != null ? resultMatrix : "—";
        }

        /** Formats Matrix A string for table display (truncated if long). */
        public String displayMatrixA() {
            if (matrixA == null) return "—";
            return matrixA.length() > 60 ? matrixA.substring(0, 57) + "..." : matrixA;
        }

        /** Formats Matrix B string for table display. */
        public String displayMatrixB() {
            if (matrixB == null || matrixB.isBlank()) return "—";
            return matrixB.length() > 60 ? matrixB.substring(0, 57) + "..." : matrixB;
        }
    }
}
