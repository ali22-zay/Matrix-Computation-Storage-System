package matrix.core;

/**
 * SquareMatrix extends Matrix to support operations only valid on square matrices,
 * specifically the determinant calculation.
 *
 * Demonstrates OOP Inheritance: inherits all Matrix capabilities and adds specialisation.
 */
public class SquareMatrix extends Matrix {

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a SquareMatrix. Throws if data is not square.
     */
    public SquareMatrix(double[][] data) {
        super(data);
        if (data.length != data[0].length)
            throw new IllegalArgumentException(
                "SquareMatrix requires equal rows and columns (got "
                + data.length + "×" + data[0].length + ").");
    }

    /**
     * Creates a zero-initialized SquareMatrix of given size.
     */
    public SquareMatrix(int size) {
        super(size, size);
    }

    // ─── Determinant ──────────────────────────────────────────────────────────

    /**
     * Computes the determinant of this square matrix using LU decomposition
     * (Gaussian elimination with partial pivoting).
     *
     * @return determinant as a double
     */
    public double determinant() {
        int n = getRows();
        double[][] a = getData();   // work on a deep copy
        double det = 1.0;
        int swaps = 0;

        for (int col = 0; col < n; col++) {
            // Partial pivoting: find row with largest absolute value in this column
            int pivotRow = col;
            double maxVal = Math.abs(a[col][col]);
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(a[row][col]) > maxVal) {
                    maxVal = Math.abs(a[row][col]);
                    pivotRow = row;
                }
            }

            if (Math.abs(a[pivotRow][col]) < 1e-12) {
                return 0.0;   // Singular matrix
            }

            // Swap rows if needed
            if (pivotRow != col) {
                double[] tmp = a[col];
                a[col] = a[pivotRow];
                a[pivotRow] = tmp;
                swaps++;
            }

            det *= a[col][col];

            // Eliminate below
            for (int row = col + 1; row < n; row++) {
                double factor = a[row][col] / a[col][col];
                for (int k = col; k < n; k++)
                    a[row][k] -= factor * a[col][k];
            }
        }

        // Odd number of swaps negates the determinant
        if (swaps % 2 != 0) det = -det;

        // Round near-zero result
        return (Math.abs(det) < 1e-10) ? 0.0 : det;
    }

    // ─── Factory ──────────────────────────────────────────────────────────────

    /**
     * Converts a general Matrix to a SquareMatrix (if it is square).
     */
    public static SquareMatrix from(Matrix m) {
        if (m.getRows() != m.getCols())
            throw new IllegalArgumentException("Matrix is not square.");
        return new SquareMatrix(m.getData());
    }

    // ─── Override toString ────────────────────────────────────────────────────

    @Override
    public String toString() {
        return super.toString();
    }
}
