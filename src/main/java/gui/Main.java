package gui;

import db.DatabaseManager;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Application entry point. Verifies the MySQL connection through the
 * {@link StartupDialog} splash, then opens the {@link LoginFrame}.
 */
public class Main {

    public static void main(String[] args) {
        GuiUtil.installNativeLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            StartupDialog splash = new StartupDialog(null);

            new Thread(() -> {
                splash.setStatus("Connecting to MySQL...");
                if (StartupDialog.testDbConnection()) {
                    splash.setStatus("Connected. Launching...");
                    splash.completeOk();
                    SwingUtilities.invokeLater(Main::openLogin);
                } else {
                    splash.completeWithError(
                        "Could not reach MySQL at localhost:3306. "
                      + "Make sure XAMPP MySQL is running and that you have "
                      + "applied scripts/setup_database.sql.");
                    if (!StartupDialog.promptRetryOrExit(null, "Database unavailable.")) {
                        System.exit(1);
                    }
                    main(args); // user clicked Retry
                }
            }, "db-check").start();

            splash.runStartupCheck();
        });
    }

    private static void openLogin() {
        // LoginFrame is responsible for spawning MainShell on success.
        new LoginFrame().setVisible(true);
    }
}