package matrix.ui;

import matrix.db.MatrixOperationDAO;
import matrix.db.MatrixOperationDAO.OperationRecord;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Modal dialog showing full calculation history from the database.
 *
 * Columns: ID | Operation | Size | Matrix A | Matrix B | Result | Timestamp
 */
public class HistoryDialog extends JDialog {

    // ─── Colors (shared with main app) ───────────────────────────────────────
    private static final Color C_BG      = new Color(0x0F0F1A);
    private static final Color C_CARD    = new Color(0x16213E);
    private static final Color C_ACCENT1 = new Color(0x7C3AED);
    private static final Color C_ACCENT2 = new Color(0x06B6D4);
    private static final Color C_ACCENT3 = new Color(0x10B981);
    private static final Color C_ERR     = new Color(0xEF4444);
    private static final Color C_TEXT    = new Color(0xE2E8F0);
    private static final Color C_MUTED   = new Color(0x94A3B8);
    private static final Color C_BORDER  = new Color(0x2D3748);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    private static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_MONO  = new Font("Consolas", Font.PLAIN, 12);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    // ─── State ────────────────────────────────────────────────────────────────
    private DefaultTableModel tableModel;
    private JTable            table;
    private JLabel            lblCount;
    private JComboBox<String> filterCombo;

    // ─── Constructor ─────────────────────────────────────────────────────────

    public HistoryDialog(Frame parent) {
        super(parent, "Calculation History", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        setSize(1000, 580);
        setLocationRelativeTo(parent);
        loadHistory("ALL");
    }

    // ─── UI ───────────────────────────────────────────────────────────────────

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(C_BG);
        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildTable(),   BorderLayout.CENTER);
        root.add(buildFooter(),  BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x1E1B4B),
                                                     getWidth(), 0, new Color(0x0C4A6E));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(C_ACCENT1);
                g2.fillRect(0, getHeight()-2, getWidth(), 2);
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 64));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel title = new JLabel("📋  Calculation History");
        title.setFont(FONT_TITLE);
        title.setForeground(Color.WHITE);

        // Filter dropdown
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        JLabel filterLbl = new JLabel("Filter:");
        filterLbl.setFont(FONT_LABEL);
        filterLbl.setForeground(C_TEXT);

        filterCombo = new JComboBox<>(
            new String[]{"ALL","ADD","SUBTRACT","MULTIPLY","TRANSPOSE","DETERMINANT"});
        filterCombo.setFont(FONT_BODY);
        filterCombo.setBackground(C_CARD);
        filterCombo.setForeground(C_TEXT);
        filterCombo.setPreferredSize(new Dimension(150, 30));
        filterCombo.addActionListener(e ->
            loadHistory((String) filterCombo.getSelectedItem()));

        JButton btnRefresh = makeButton("⟳  Refresh", C_ACCENT2);
        btnRefresh.addActionListener(e ->
            loadHistory((String) filterCombo.getSelectedItem()));

        controls.add(filterLbl);
        controls.add(filterCombo);
        controls.add(btnRefresh);

        lblCount = new JLabel("0 records");
        lblCount.setFont(FONT_SMALL);
        lblCount.setForeground(C_MUTED);
        lblCount.setBorder(new EmptyBorder(0, 12, 0, 0));
        controls.add(lblCount);

        header.add(title,    BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Operation", "Size", "Matrix A", "Matrix B", "Result", "Timestamp"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setBackground(C_CARD);
        table.setForeground(C_TEXT);
        table.setGridColor(C_BORDER);
        table.setFont(FONT_MONO);
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setSelectionBackground(new Color(C_ACCENT1.getRed(), C_ACCENT1.getGreen(), C_ACCENT1.getBlue(), 80));
        table.setSelectionForeground(Color.WHITE);

        // Column widths
        int[] widths = {50, 110, 100, 180, 180, 180, 160};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            if (i == 0 || i == 1 || i == 2)
                table.getColumnModel().getColumn(i).setMaxWidth(widths[i] + 20);
        }

        // Header style
        JTableHeader th = table.getTableHeader();
        th.setPreferredSize(new Dimension(0, 32));
        th.setReorderingAllowed(false);
        th.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(new Color(0x1D4ED8)); // Blue background
                setForeground(Color.WHITE);         // White text
                setFont(FONT_LABEL);
                setHorizontalAlignment(JLabel.CENTER);
                setBorder(new MatteBorder(0, 0, 1, 1, C_BORDER));
                return this;
            }
        });

        // Row striping renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setFont(col == 1 ? FONT_LABEL : FONT_MONO);
                if (!sel) {
                    setBackground(row % 2 == 0 ? C_CARD : new Color(0x1E293B));
                    setForeground(col == 1 ? C_ACCENT3 : C_TEXT);
                }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // Double-click to show detail
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) showDetail();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setBackground(C_CARD);
        scroll.getViewport().setBackground(C_CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        footer.setBackground(new Color(0x0A0A14));
        footer.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDER));

        JButton btnDetail = makeButton("👁  View Detail", C_ACCENT2);
        btnDetail.addActionListener(e -> showDetail());

        JButton btnClose = makeButton("Close", C_ERR);
        btnClose.addActionListener(e -> dispose());

        footer.add(btnDetail);
        footer.add(btnClose);
        return footer;
    }

    // ─── Data ────────────────────────────────────────────────────────────────

    private void loadHistory(String filter) {
        SwingWorker<List<OperationRecord>, Void> worker = new SwingWorker<>() {
            @Override protected List<OperationRecord> doInBackground() {
                return MatrixOperationDAO.getHistory(500);
            }
            @Override protected void done() {
                try {
                    List<OperationRecord> all = get();
                    tableModel.setRowCount(0);
                    int count = 0;
                    for (OperationRecord r : all) {
                        if (!filter.equals("ALL") && !r.operationType().equals(filter)) continue;
                        tableModel.addRow(new Object[]{
                            r.id(),
                            r.operationType(),
                            (r.matrixB() != null && !r.matrixB().isBlank() ? "A:" + r.rowsA() + "×" + r.colsA() + " B:" + r.rowsB() + "×" + r.colsB() : r.rowsA() + "×" + r.colsA()),
                            r.displayMatrixA(),
                            r.displayMatrixB(),
                            r.displayResult(),
                            r.createdAt().toString().replace(".0", "")
                        });
                        count++;
                    }
                    lblCount.setText(count + " record" + (count == 1 ? "" : "s"));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(HistoryDialog.this,
                        "Error loading history: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /** Shows a detail popup for the selected row */
    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a row first.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String id  = tableModel.getValueAt(row, 0).toString();
        String op  = tableModel.getValueAt(row, 1).toString();
        String sz  = tableModel.getValueAt(row, 2).toString();
        String mA  = tableModel.getValueAt(row, 3).toString();
        String mB  = tableModel.getValueAt(row, 4).toString();
        String res = tableModel.getValueAt(row, 5).toString();
        String ts  = tableModel.getValueAt(row, 6).toString();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(C_CARD);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        addDetailRow(panel, gc, 0, "ID",          id);
        addDetailRow(panel, gc, 1, "Operation",   op);
        addDetailRow(panel, gc, 2, "Size",        sz);
        addDetailRow(panel, gc, 3, "Matrix A",    mA);
        addDetailRow(panel, gc, 4, "Matrix B",    mB);
        addDetailRow(panel, gc, 5, "Result",      res);
        addDetailRow(panel, gc, 6, "Timestamp",   ts);

        JScrollPane sp = new JScrollPane(panel);
        sp.setPreferredSize(new Dimension(550, 280));
        sp.setBorder(null);

        JDialog detail = new JDialog(this, "Record #" + id + " — " + op, true);
        detail.setContentPane(sp);
        detail.pack();
        detail.setLocationRelativeTo(this);
        detail.getContentPane().setBackground(C_CARD);
        detail.setVisible(true);
    }

    private void addDetailRow(JPanel p, GridBagConstraints gc, int row, String key, String val) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel lbl = new JLabel(key + ":");
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(C_ACCENT2);
        p.add(lbl, gc);

        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        JTextField tf = new JTextField(val);
        tf.setFont(FONT_MONO);
        tf.setForeground(C_TEXT);
        tf.setBackground(new Color(0x0F1729));
        tf.setBorder(new CompoundBorder(
            new LineBorder(C_BORDER, 1, true), new EmptyBorder(3, 6, 3, 6)));
        tf.setEditable(false);
        p.add(tf, gc);
        gc.fill = GridBagConstraints.NONE;
    }

    // ─── Widget factory ───────────────────────────────────────────────────────

    private JButton makeButton(String label, Color accent) {
        JButton btn = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() || getModel().isPressed()
                    ? accent : new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(accent);
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BODY);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
