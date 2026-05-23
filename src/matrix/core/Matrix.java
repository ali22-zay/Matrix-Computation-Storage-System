package matrix.core;

/**
 * Base Matrix class implementing core matrix operations.
 * Demonstrates OOP principles: encapsulation, data hiding, and abstraction.
 */
public class Matrix {

    // ─── Fields (encapsulated) ────────────────────────────────────────────────
    private final double[][] data;
    private final int rows;
    private final int cols;

    // ─── Constructors ─────────────────────────────────────────────────────────

    /**
     * Creates a Matrix from a 2D double array (deep-copied for immutability).
     */
    public Matrix(double[][] data) {
        if (data == null || data.length == 0)
            throw new IllegalArgumentException("Matrix data cannot be null or empty.");
        this.rows = data.length;
        this.cols = data[0].length;
        // Deep copy to ensure encapsulation
        this.data = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            if (data[i].length != cols)
                throw new IllegalArgumentException("All rows must have the same number of columns.");
            System.arraycopy(data[i], 0, this.data[i], 0, cols);
        }
    }

    /**
     * Creates a zero-initialized matrix of given dimensions.
     */
    public Matrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0)
            throw new IllegalArgumentException("Dimensions must be positive.");
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int getRows()  { return rows; }
    public int getCols()  { return cols; }

    /**
     * Returns a deep copy of the internal data array.
     */
    public double[][] getData() {
        double[][] copy = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            System.arraycopy(data[i], 0, copy[i], 0, cols);
        return copy;
    }

    public double get(int row, int col) {
        validateIndex(row, col);
        return data[row][col];
    }

    protected void set(int row, int col, double value) {
        validateIndex(row, col);
        data[row][col] = value;
    }

    // ─── Core Operations ──────────────────────────────────────────────────────

    /**
     * Adds this matrix to another matrix.
     * @return a new Matrix containing the element-wise sum
     */
    public Matrix add(Matrix other) {
        checkSameDimensions(other);
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result.set(i, j, this.data[i][j] + other.data[i][j]);
        return result;
    }

    /**
     * Subtracts another matrix from this matrix.
     * @return a new Matrix containing the element-wise difference
     */
    public Matrix subtract(Matrix other) {
        checkSameDimensions(other);
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result.set(i, j, this.data[i][j] - other.data[i][j]);
        return result;
    }

    /**
     * Multiplies this matrix by another matrix (standard matrix product).
     * @return a new Matrix (this.rows × other.cols)
     */
    public Matrix multiply(Matrix other) {
        if (this.cols != other.rows)
            throw new IllegalArgumentException(
                "Cannot multiply: columns of A (" + this.cols + ") ≠ rows of B (" + other.rows + ").");
        Matrix result = new Matrix(this.rows, other.cols);
        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < other.cols; j++) {
                double sum = 0;
                for (int k = 0; k < this.cols; k++)
                    sum += this.data[i][k] * other.data[k][j];
                result.set(i, j, sum);
            }
        return result;
    }

    /**
     * Returns the transpose of this matrix.
     * @return a new Matrix (cols × rows)
     */
    public Matrix transpose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result.set(j, i, data[i][j]);
        return result;
    }

    /**
     * Multiplies all elements by a scalar.
     */
    public Matrix scalarMultiply(double scalar) {
        Matrix result = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result.set(i, j, data[i][j] * scalar);
        return result;
    }

    // ─── Validation Helpers ───────────────────────────────────────────────────

    protected void checkSameDimensions(Matrix other) {
        if (this.rows != other.rows || this.cols != other.cols)
            throw new IllegalArgumentException(
                "Matrix dimensions must match: ("
                + this.rows + "×" + this.cols + ") vs ("
                + other.rows + "×" + other.cols + ").");
    }

    private void validateIndex(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols)
            throw new IndexOutOfBoundsException(
                "Index (" + row + "," + col + ") out of bounds for " + rows + "×" + cols + " matrix.");
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    /**
     * Formats this matrix as a compact string, e.g. "[[1.0,2.0],[3.0,4.0]]"
     */
    public String toCompactString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rows; i++) {
            sb.append("[");
            for (int j = 0; j < cols; j++) {
                // Format: trim unnecessary trailing zeros
                double v = data[i][j];
                sb.append((v == (long) v) ? String.valueOf((long) v) : String.valueOf(v));
                if (j < cols - 1) sb.append(",");
            }
            sb.append("]");
            if (i < rows - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Pretty-prints the matrix for display purposes.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            sb.append("[ ");
            for (int j = 0; j < cols; j++) {
                double v = data[i][j];
                String fmt = (v == (long) v) ? String.valueOf((long) v) : String.format("%.4f", v);
                sb.append(String.format("%10s", fmt));
                if (j < cols - 1) sb.append("  ");
            }
            sb.append(" ]\n");
        }
        return sb.toString().stripTrailing();
    }

    /**
     * Parses a compact string produced by {@link #toCompactString()} back into a Matrix.
     */
    public static Matrix fromCompactString(String s) {
        s = s.trim();
        if (s.startsWith("[")) s = s.substring(1);
        if (s.endsWith("]")) s = s.substring(0, s.length() - 1);
        String[] rowStrs = s.split("\\],\\[");
        int rowCount = rowStrs.length;
        double[][] parsed = null;
        for (int i = 0; i < rowCount; i++) {
            String row = rowStrs[i].replace("[", "").replace("]", "");
            String[] vals = row.split(",");
            if (parsed == null) parsed = new double[rowCount][vals.length];
            for (int j = 0; j < vals.length; j++)
                parsed[i][j] = Double.parseDouble(vals[j].trim());
        }
        return new Matrix(parsed);
    }
}
