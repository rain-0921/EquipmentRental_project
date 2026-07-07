package gui;

import db.DatabaseManager;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Splash-style startup window. Verifies the MySQL connection before
 * the main window opens so the user gets a clear, actionable error
 * instead of a generic exception trace.
 */
public class StartupDialog extends JDialog {

    private final JLabel statusLabel;
    private final JProgressBar progress;

    public StartupDialog(JFrame owner) {
        super(owner, "Smart Equipment Rental System", true);
        setUndecorated(true);
        setSize(new Dimension(460, 180));
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Smart Equipment Rental System", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        statusLabel = new JLabel("Connecting to MySQL...", SwingConstants.CENTER);
        progress = new JProgressBar();
        progress.setIndeterminate(true);

        root.add(title, BorderLayout.NORTH);
        root.add(statusLabel, BorderLayout.CENTER);
        root.add(progress, BorderLayout.SOUTH);
        setContentPane(root);
    }

    public void runStartupCheck() {
        setVisible(true); // blocks until dispose()
    }

    /** Called from a worker thread - updates UI via invokeLater. */
    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    public void completeOk() {
        SwingUtilities.invokeLater(() -> dispose());
    }

    public void completeWithError(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Failed.");
            progress.setIndeterminate(false);
            GuiUtil.showError(this, "Database connection failed", message);
            dispose();
        });
    }

    /** Convenience: a "Retry" / "Exit" dialog after a failed start. */
    public static boolean promptRetryOrExit(java.awt.Component parent, String message) {
        int choice = JOptionPane.showConfirmDialog(
            parent,
            message + "\n\nMake sure MySQL is running and that you have run "
                    + "scripts/setup_database.sql.\n\nWould you like to retry?",
            "Connection problem",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    public static boolean testDbConnection() {
        return DatabaseManager.getInstance().testConnection();
    }
}