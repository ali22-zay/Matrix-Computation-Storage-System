package matrix.ui;

import matrix.core.Matrix;
import matrix.core.SquareMatrix;
import matrix.db.DatabaseManager;
import matrix.db.MatrixOperationDAO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;

/**
 * Main application window — OOP-based Matrix Computation & Storage System.
 *
 * Layout:
 *   ┌──────────────────────────────────────────────────────────┐
 *   │  Header (gradient title bar)                             │
 *   ├──────────────────┬───────────────────────────────────────┤
 *   │  Left Panel      │  Right Panel                          │
 *   │  · Matrix A grid │  · Result JTable                      │
 *   │  · Matrix B grid │  · Scalar result label                │
 *   │  · Operation     │  · Status bar                         │
 *   │    buttons       │                                        │
 *   └──────────────────┴───────────────────────────────────────┘
 */
public class MatrixApp extends JFrame {

    // ─── Constants ────────────────────────────────────────────────────────────
    private static final int MAX_SIZE  = 5;   // max matrix dimension
    private static final int DEF_SIZE  = 3;   // default grid size shown
    private static final Color C_BG        = new Color(0x0F0F1A);
    private static final Color C_PANEL     = new Color(0x1A1A2E);
    private static final Color C_CARD      = new Color(0x16213E);
    private static final Color C_ACCENT1   = new Color(0x7C3AED);   // purple
    private static final Color C_ACCENT2   = new Color(0x06B6D4);   // cyan
    private static final Color C_ACCENT3   = new Color(0x10B981);   // green
    private static final Color C_WARN      = new Color(0xF59E0B);   // amber
    private static final Color C_ERR       = new Color(0xEF4444);   // red
    private static final Color C_TEXT      = new Color(0xE2E8F0);
    private static final Color C_MUTED     = new Color(0x94A3B8);
    private static final Color C_BORDER    = new Color(0x2D3748);
    private static final Color C_CELL_BG   = new Color(0x1E293B);
    private static final Color C_CELL_FG   = new Color(0xF1F5F9);

    private static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONT_LABEL  = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_MONO   = new Font("Consolas", Font.PLAIN, 14);
    private static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

    // ─── State ────────────────────────────────────────────────────────────────
    private int currentSize = DEF_SIZE;
    private boolean dbConnected = false;

    // ─── Widgets ──────────────────────────────────────────────────────────────
    private JTextField[][] cellsA;
    private JTextField[][] cellsB;
    private JLabel         lblScalar;
    private JLabel         lblStatus;
    private JLabel         lblDbStatus;
    private JLabel         lblSavedCount;
    private DefaultTableModel resultTableModel;
    private JTable         resultTable;
    private JPanel         tableWrapper;
    private JPanel         centeringPanel;
    private JLabel         lblPlaceholder;
    private JPanel         gridPanelA;
    private JPanel         gridPanelB;
    private JSpinner       sizeSpinner;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public MatrixApp() {
        super("OOP-based Matrix Computation & Storage System");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { onExit(); }
        });

        applyGlobalLAF();
        buildUI();
        pack();
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        // Async DB connect
        SwingUtilities.invokeLater(this::connectToDatabase);
    }

    // ─── Look-and-feel ────────────────────────────────────────────────────────

    private void applyGlobalLAF() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        UIManager.put("Panel.background", C_BG);
        UIManager.put("ScrollPane.background", C_PANEL);
        UIManager.put("Table.background", C_CARD);
        UIManager.put("Table.foreground", C_TEXT);
        UIManager.put("Table.gridColor", C_BORDER);
        UIManager.put("TableHeader.background", C_ACCENT1);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("ScrollBar.thumb", C_ACCENT1);
        UIManager.put("ScrollBar.track", C_PANEL);
    }

    // ─── UI Assembly ──────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(C_BG);

        root.add(buildHeader(),     BorderLayout.NORTH);
        root.add(buildMain(),       BorderLayout.CENTER);
        root.add(buildStatusBar(),  BorderLayout.SOUTH);

        setContentPane(root);
    }

    /** Gradient title header */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x0F172A),
                                                     getWidth(), 0, new Color(0x1E1B4B));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Bottom accent line
                GradientPaint accent = new GradientPaint(0, getHeight()-2, C_ACCENT1,
                                                         getWidth(), getHeight()-2, C_ACCENT2);
                g2.setPaint(accent);
                g2.fillRect(0, getHeight()-2, getWidth(), 2);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(new EmptyBorder(0, 24, 0, 24));

        GridBagConstraints gc = new GridBagConstraints();

        // Left component (for balance)
        JPanel leftPlaceholder = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPlaceholder.setOpaque(false);
        JLabel lblLeft = new JLabel("⬡ MATRIX CORE");
        lblLeft.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLeft.setForeground(C_MUTED);
        leftPlaceholder.add(lblLeft);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;
        header.add(leftPlaceholder, gc);

        // Center component (Title)
        JLabel title = new JLabel("OOP-based Matrix Computation & Storage System");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gc.gridx = 1;
        gc.weightx = 0.0;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.CENTER;
        header.add(title, gc);

        // Right component (DB status + History)
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        lblDbStatus = makePill("● DB: Connecting…", C_WARN);
        JButton btnHistory = makeGlassButton("📋  History", C_ACCENT2);
        btnHistory.setName("btn-history");
        btnHistory.addActionListener(e -> openHistory());

        right.add(lblDbStatus);
        right.add(btnHistory);

        gc.gridx = 2;
        gc.weightx = 1.0;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.EAST;
        header.add(right, gc);

        return header;
    }

    /** Main split: left (inputs + ops) | right (result) */
    private JSplitPane buildMain() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                          buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(520);
        split.setDividerSize(3);
        split.setBackground(C_BG);
        split.setBorder(null);
        split.setContinuousLayout(true);
        return split;
    }

    // ─── LEFT PANEL ───────────────────────────────────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setBackground(C_BG);
        left.setBorder(new EmptyBorder(16, 16, 12, 8));

        // Size control
        left.add(buildSizeControl(), BorderLayout.NORTH);

        // Matrix grids in scroll pane
        JPanel grids = new JPanel();
        grids.setLayout(new BoxLayout(grids, BoxLayout.Y_AXIS));
        grids.setBackground(C_BG);

        grids.add(buildMatrixInputCard("Matrix  A", true));
        grids.add(Box.createVerticalStrut(12));
        grids.add(buildMatrixInputCard("Matrix  B  (binary ops)", false));
        grids.add(Box.createVerticalStrut(12));
        grids.add(buildOperationButtons());

        JScrollPane scroll = new JScrollPane(grids);
        scroll.setBorder(null);
        scroll.setBackground(C_BG);
        scroll.getViewport().setBackground(C_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        left.add(scroll, BorderLayout.CENTER);

        return left;
    }

    private JPanel buildSizeControl() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBackground(C_BG);

        JLabel lbl = new JLabel("Matrix Size:");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(C_TEXT);

        SpinnerNumberModel model = new SpinnerNumberModel(DEF_SIZE, 1, MAX_SIZE, 1);
        sizeSpinner = new JSpinner(model);
        sizeSpinner.setPreferredSize(new Dimension(60, 30));
        styleSpinner(sizeSpinner);

        JButton btnApply = makeGlassButton("Apply", C_ACCENT1);
        btnApply.setName("btn-apply-size");
        btnApply.addActionListener(e -> applySize());

        JButton btnClear = makeGlassButton("Clear All", C_ERR);
        btnClear.setName("btn-clear");
        btnClear.addActionListener(e -> clearAll());

        p.add(lbl);
        p.add(sizeSpinner);
        p.add(btnApply);
        p.add(Box.createHorizontalStrut(6));
        p.add(btnClear);
        return p;
    }

    private JPanel buildMatrixInputCard(String title, boolean isA) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(C_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(isA ? C_ACCENT2 : C_ACCENT3);
        card.add(lbl, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(currentSize, currentSize, 6, 6));
        grid.setBackground(C_CARD);
        JTextField[][] cells = new JTextField[MAX_SIZE][MAX_SIZE];
        populateGrid(grid, cells, currentSize, isA);

        if (isA) { cellsA = cells; gridPanelA = grid; }
        else      { cellsB = cells; gridPanelB = grid; }

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private void populateGrid(JPanel grid, JTextField[][] cells, int size, boolean isA) {
        grid.removeAll();
        grid.setLayout(new GridLayout(size, size, 6, 6));
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++) {
                if (cells[i][j] == null) cells[i][j] = makeMatrixCell();
                else cells[i][j].setText("");
                cells[i][j].putClientProperty("row", i);
                cells[i][j].putClientProperty("col", j);
                cells[i][j].putClientProperty("isA", isA);
                grid.add(cells[i][j]);
            }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel buildOperationButtons() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(C_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            new EmptyBorder(12, 14, 14, 14)
        ));

        JLabel lbl = new JLabel("Operations");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(C_TEXT);
        card.add(lbl, BorderLayout.NORTH);

        // Binary operations row
        JPanel binRow = new JPanel(new GridLayout(1, 3, 8, 0));
        binRow.setBackground(C_CARD);
        binRow.add(makeOpButton("➕  Add",        "ADD",        C_ACCENT3));
        binRow.add(makeOpButton("➖  Subtract",   "SUBTRACT",   C_WARN));
        binRow.add(makeOpButton("✖  Multiply",   "MULTIPLY",   C_ACCENT1));

        // Unary operations row
        JPanel uniRow = new JPanel(new GridLayout(1, 2, 8, 0));
        uniRow.setBackground(C_CARD);
        uniRow.add(makeOpButton("⟳  Transpose",  "TRANSPOSE",  C_ACCENT2));
        uniRow.add(makeOpButton("∣A∣  Determinant","DETERMINANT",C_ERR));

        JPanel rows = new JPanel(new GridLayout(2, 1, 0, 8));
        rows.setBackground(C_CARD);
        rows.add(binRow);
        rows.add(uniRow);
        card.add(rows, BorderLayout.CENTER);
        return card;
    }

    // ─── RIGHT PANEL ──────────────────────────────────────────────────────────

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setBackground(C_BG);
        right.setBorder(new EmptyBorder(16, 8, 12, 16));

        // Result matrix table
        right.add(buildResultTableCard(), BorderLayout.CENTER);

        // Scalar result
        lblScalar = new JLabel(" ");
        lblScalar.setFont(new Font("Consolas", Font.BOLD, 22));
        lblScalar.setForeground(C_ACCENT2);
        lblScalar.setHorizontalAlignment(SwingConstants.CENTER);
        lblScalar.setBorder(new EmptyBorder(6, 0, 6, 0));
        right.add(lblScalar, BorderLayout.SOUTH);

        return right;
    }

    private JPanel buildResultTableCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(C_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        JLabel lbl = new JLabel("Result");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(C_ACCENT1);
        card.add(lbl, BorderLayout.NORTH);

        resultTableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        resultTable = new JTable(resultTableModel);
        styleTable(resultTable);

        // Wrapper to hold header + table cells
        tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.add(resultTable.getTableHeader(), BorderLayout.NORTH);
        tableWrapper.add(resultTable, BorderLayout.CENTER);

        // Centering container
        centeringPanel = new JPanel(new GridBagLayout());
        centeringPanel.setBackground(C_CARD);
        
        lblPlaceholder = new JLabel("Perform an operation to view result");
        lblPlaceholder.setFont(FONT_LABEL);
        lblPlaceholder.setForeground(C_MUTED);
        
        centeringPanel.add(lblPlaceholder);
        centeringPanel.add(tableWrapper);
        
        tableWrapper.setVisible(false); // Hide table initially

        JScrollPane scroll = new JScrollPane(centeringPanel);
        scroll.setBorder(new LineBorder(C_BORDER, 1));
        scroll.setBackground(C_CARD);
        scroll.getViewport().setBackground(C_CARD);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ─── STATUS BAR ───────────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x0A0A14));
        bar.setBorder(new CompoundBorder(
            new MatteBorder(1, 0, 0, 0, C_BORDER),
            new EmptyBorder(4, 16, 4, 16)
        ));

        lblStatus = new JLabel("Ready");
        lblStatus.setFont(FONT_SMALL);
        lblStatus.setForeground(C_MUTED);

        lblSavedCount = new JLabel("Saved: –");
        lblSavedCount.setFont(FONT_SMALL);
        lblSavedCount.setForeground(C_MUTED);

        bar.add(lblStatus,     BorderLayout.WEST);
        bar.add(lblSavedCount, BorderLayout.EAST);
        return bar;
    }

    // ─── Database ─────────────────────────────────────────────────────────────

    private void connectToDatabase() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override protected Boolean doInBackground() {
                try {
                    DatabaseManager.getConnection();
                    return true;
                } catch (Exception e) {
                    System.err.println("DB connect failed: " + e.getMessage());
                    return false;
                }
            }
            @Override protected void done() {
                try {
                    dbConnected = get();
                    if (dbConnected) {
                        lblDbStatus.setText("● DB: Connected");
                        lblDbStatus.setForeground(C_ACCENT3);
                        setStatus("Connected to " + DatabaseManager.getConnectionInfo());
                        refreshSavedCount();
                    } else {
                        lblDbStatus.setText("● DB: Offline");
                        lblDbStatus.setForeground(C_ERR);
                        setStatus("Database offline — results will not be saved.");
                    }
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }

    // ─── Operations ───────────────────────────────────────────────────────────

    private void performOperation(String op) {
        try {
            Matrix a = readMatrix(cellsA);
            Matrix result = null;
            Double scalar = null;

            switch (op) {
                case "ADD"         -> { Matrix b = readMatrix(cellsB); result = a.add(b);       saveOp(op, a, b, result, null); }
                case "SUBTRACT"    -> { Matrix b = readMatrix(cellsB); result = a.subtract(b);  saveOp(op, a, b, result, null); }
                case "MULTIPLY"    -> { Matrix b = readMatrix(cellsB); result = a.multiply(b);  saveOp(op, a, b, result, null); }
                case "TRANSPOSE"   -> { result = a.transpose();                                  saveOp(op, a, null, result, null); }
                case "DETERMINANT" -> {
                    SquareMatrix sq = SquareMatrix.from(a);
                    scalar = sq.determinant();
                    saveOp(op, a, null, null, scalar);
                }
            }

            displayResult(result, scalar, op);
            setStatus("✓  " + op + " completed and saved.");
            refreshSavedCount();

        } catch (IllegalArgumentException ex) {
            showError("Operation Error", ex.getMessage());
            setStatus("✗  " + ex.getMessage());
        }
    }

    private void saveOp(String type, Matrix a, Matrix b, Matrix result, Double scalar) {
        if (!dbConnected) return;
        SwingWorker<Void, Void> w = new SwingWorker<>() {
            @Override protected Void doInBackground() {
                MatrixOperationDAO.save(type, a, b, result, scalar);
                return null;
            }
        };
        w.execute();
    }

    // ─── Display ──────────────────────────────────────────────────────────────

    private void displayResult(Matrix m, Double scalar, String op) {
        resultTableModel.setRowCount(0);
        resultTableModel.setColumnCount(0);
        lblScalar.setText(" ");

        if (scalar == null && m == null) {
            lblPlaceholder.setVisible(true);
            tableWrapper.setVisible(false);
            centeringPanel.revalidate();
            centeringPanel.repaint();
            return;
        }

        lblPlaceholder.setVisible(false);
        tableWrapper.setVisible(true);

        if (scalar != null) {
            lblScalar.setText("det(A) = " + formatScalar(scalar));
            resultTableModel.addColumn("Result Value");
            resultTableModel.addRow(new Object[]{formatScalar(scalar)});
            
            int colWidth = 150;
            TableColumn col = resultTable.getColumnModel().getColumn(0);
            col.setPreferredWidth(colWidth);
            col.setMinWidth(colWidth);
            col.setMaxWidth(colWidth);
            
            resultTable.setPreferredSize(new Dimension(colWidth, resultTable.getRowHeight()));
            centeringPanel.revalidate();
            centeringPanel.repaint();
            return;
        }

        // Build column headers Col 1, Col 2 …
        for (int j = 0; j < m.getCols(); j++)
            resultTableModel.addColumn("Col " + (j + 1));

        for (int i = 0; i < m.getRows(); i++) {
            Object[] row = new Object[m.getCols()];
            for (int j = 0; j < m.getCols(); j++) {
                double v = m.get(i, j);
                row[j] = (v == (long) v) ? String.valueOf((long) v) : String.format("%.4f", v);
            }
            resultTableModel.addRow(row);
        }

        // Set column widths and table size to fit exactly
        int colWidth = 80;
        int tableWidth = m.getCols() * colWidth;
        int tableHeight = m.getRows() * resultTable.getRowHeight();
        
        for (int j = 0; j < m.getCols(); j++) {
            TableColumn col = resultTable.getColumnModel().getColumn(j);
            col.setPreferredWidth(colWidth);
            col.setMinWidth(colWidth);
            col.setMaxWidth(colWidth);
        }
        
        resultTable.setPreferredSize(new Dimension(tableWidth, tableHeight));
        centeringPanel.revalidate();
        centeringPanel.repaint();
    }

    private String formatScalar(double v) {
        return (v == (long) v) ? String.valueOf((long) v) : String.format("%.6f", v);
    }

    // ─── Grid management ─────────────────────────────────────────────────────

    private void applySize() {
        currentSize = (int) sizeSpinner.getValue();
        populateGrid(gridPanelA, cellsA, currentSize, true);
        populateGrid(gridPanelB, cellsB, currentSize, false);
        displayResult(null, null, null);
        setStatus("Grid resized to " + currentSize + "×" + currentSize);
    }

    private void clearAll() {
        for (int i = 0; i < MAX_SIZE; i++)
            for (int j = 0; j < MAX_SIZE; j++) {
                if (cellsA[i][j] != null) cellsA[i][j].setText("");
                if (cellsB[i][j] != null) cellsB[i][j].setText("");
            }
        displayResult(null, null, null);
        setStatus("Cleared.");
    }

    // ─── History dialog ───────────────────────────────────────────────────────

    private void openHistory() {
        if (!dbConnected) {
            showError("Database Offline", "Cannot retrieve history — database is not connected.");
            return;
        }
        new HistoryDialog(this).setVisible(true);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Matrix readMatrix(JTextField[][] cells) {
        double[][] data = new double[currentSize][currentSize];
        for (int i = 0; i < currentSize; i++)
            for (int j = 0; j < currentSize; j++) {
                String txt = cells[i][j].getText().trim();
                if (txt.isEmpty()) txt = "0";
                try {
                    data[i][j] = Double.parseDouble(txt);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Invalid value at [" + (i+1) + "," + (j+1) + "]: \"" + txt + "\"");
                }
            }
        return new Matrix(data);
    }

    private void refreshSavedCount() {
        if (!dbConnected) return;
        SwingWorker<Integer, Void> w = new SwingWorker<>() {
            @Override protected Integer doInBackground() { return MatrixOperationDAO.countAll(); }
            @Override protected void done() {
                try { lblSavedCount.setText("Saved: " + get() + " ops"); }
                catch (Exception ignored) {}
            }
        };
        w.execute();
    }

    private void setStatus(String msg) {
        lblStatus.setText(msg);
    }

    private void showError(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    private void onExit() {
        DatabaseManager.close();
        dispose();
        System.exit(0);
    }

    // ─── Keyboard Navigation Listener ──────────────────────────────────────────

    private final java.awt.event.KeyListener cellNavigationListener = new java.awt.event.KeyAdapter() {
        @Override public void keyPressed(java.awt.event.KeyEvent e) {
            JTextField source = (JTextField) e.getSource();
            Integer r = (Integer) source.getClientProperty("row");
            Integer c = (Integer) source.getClientProperty("col");
            Boolean isA = (Boolean) source.getClientProperty("isA");
            if (r == null || c == null || isA == null) return;

            JTextField[][] currentGrid = isA ? cellsA : cellsB;
            JTextField[][] otherGrid = isA ? cellsB : cellsA;

            int code = e.getKeyCode();
            
            if (code == java.awt.event.KeyEvent.VK_ENTER) {
                int nextCol = c + 1;
                int nextRow = r;
                if (nextCol >= currentSize) {
                    nextCol = 0;
                    nextRow = r + 1;
                }
                if (nextRow < currentSize) {
                    currentGrid[nextRow][nextCol].requestFocusInWindow();
                    currentGrid[nextRow][nextCol].selectAll();
                } else {
                    otherGrid[0][0].requestFocusInWindow();
                    otherGrid[0][0].selectAll();
                }
                e.consume();
            } 
            else if (code == java.awt.event.KeyEvent.VK_RIGHT) {
                if (c + 1 < currentSize) {
                    currentGrid[r][c + 1].requestFocusInWindow();
                    currentGrid[r][c + 1].selectAll();
                    e.consume();
                }
            } 
            else if (code == java.awt.event.KeyEvent.VK_LEFT) {
                if (c - 1 >= 0) {
                    currentGrid[r][c - 1].requestFocusInWindow();
                    currentGrid[r][c - 1].selectAll();
                    e.consume();
                }
            } 
            else if (code == java.awt.event.KeyEvent.VK_UP) {
                if (r - 1 >= 0) {
                    currentGrid[r - 1][c].requestFocusInWindow();
                    currentGrid[r - 1][c].selectAll();
                    e.consume();
                }
            } 
            else if (code == java.awt.event.KeyEvent.VK_DOWN) {
                if (r + 1 < currentSize) {
                    currentGrid[r + 1][c].requestFocusInWindow();
                    currentGrid[r + 1][c].selectAll();
                    e.consume();
                }
            }
        }
    };

    // ─── Widget factories ─────────────────────────────────────────────────────

    private JTextField makeMatrixCell() {
        JTextField tf = new JTextField("0");
        tf.setFont(FONT_MONO);
        tf.setForeground(C_CELL_FG);
        tf.setBackground(C_CELL_BG);
        tf.setCaretColor(C_ACCENT2);
        tf.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1, true),
            new EmptyBorder(4, 6, 4, 6)
        ));
        tf.setHorizontalAlignment(JTextField.CENTER);
        tf.setPreferredSize(new Dimension(70, 38));
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                tf.selectAll();
                tf.setBorder(new CompoundBorder(
                    new LineBorder(C_ACCENT1, 1, true),
                    new EmptyBorder(4, 6, 4, 6)));
            }
            @Override public void focusLost(FocusEvent e) {
                tf.setBorder(new CompoundBorder(
                    new LineBorder(C_BORDER, 1, true),
                    new EmptyBorder(4, 6, 4, 6)));
            }
        });
        tf.addKeyListener(cellNavigationListener);
        return tf;
    }

    private JButton makeOpButton(String label, String op, Color accent) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()
                    ? accent.darker()
                    : getModel().isRollover()
                        ? accent
                        : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 60);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setName("btn-" + op.toLowerCase());
        btn.setFont(FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(10, 8, 10, 8));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> performOperation(op));
        return btn;
    }

    private JButton makeGlassButton(String label, Color accent) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()
                    ? accent.darker()
                    : getModel().isRollover()
                        ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 180)
                        : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(accent);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_SMALL);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel makePill(String text, Color color) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.setColor(color);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, getHeight()-1, getHeight()-1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(color);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(4, 12, 4, 12));
        return lbl;
    }

    private void styleTable(JTable table) {
        table.setBackground(C_CARD);
        table.setForeground(C_TEXT);
        table.setGridColor(C_BORDER);
        table.setFont(FONT_MONO);
        table.setRowHeight(32);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(C_ACCENT1.getRed(), C_ACCENT1.getGreen(), C_ACCENT1.getBlue(), 80));
        table.setSelectionForeground(Color.WHITE);
        
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(new Color(0x1D4ED8)); // Beautiful Royal Blue (Tailwind Blue-700)
                setForeground(Color.WHITE);
                setFont(FONT_LABEL);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(new MatteBorder(0, 0, 1, 1, C_BORDER));
                return this;
            }
        });
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(C_CELL_BG);
        spinner.setForeground(C_TEXT);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBackground(C_CELL_BG);
            de.getTextField().setForeground(C_TEXT);
            de.getTextField().setCaretColor(C_ACCENT2);
            de.getTextField().setFont(FONT_BODY);
        }
    }
}
