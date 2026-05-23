package matrix;

import matrix.ui.MatrixApp;

import javax.swing.*;

/**
 * Application entry point.
 *
 * Launches the MatrixApp JFrame on the Swing Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {

        // ── Optional: force hardware acceleration on Windows ──────────────
        System.setProperty("sun.java2d.opengl", "true");

        // ── Launch on the EDT ─────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            try {
                // Try Nimbus for a more modern baseline; fall back gracefully
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception ignored) {
                // Keep system LAF
            }

            MatrixApp app = new MatrixApp();
            app.setVisible(true);
        });
    }
}
