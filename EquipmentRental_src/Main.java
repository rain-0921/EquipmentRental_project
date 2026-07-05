import gui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }

        // Catch anything that goes wrong on the Swing event thread (e.g. DB
        // connection failures) and show it instead of silently freezing/exiting.
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to start the application:\n" + ex,
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        });

        SwingUtilities.invokeLater(Main::showLoadingThenLaunch);
    }

    private static void showLoadingThenLaunch() {
        JDialog loading = new JDialog((Frame) null, "Please wait", false);
        JLabel label = new JLabel("  Connecting to database...  ", SwingConstants.CENTER);
        label.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        loading.getContentPane().add(label);
        loading.pack();
        loading.setLocationRelativeTo(null);
        loading.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        loading.setVisible(true);

        // Do the slow work (DB connection + schema init happens inside MainFrame's
        // constructor via RentalSystem) on a background thread so the UI never freezes.
        SwingWorker<MainFrame, Void> worker = new SwingWorker<>() {
            @Override
            protected MainFrame doInBackground() {
                return new MainFrame(); // RentalSystem() connects to the DB here
            }

            @Override
            protected void done() {
                loading.dispose();
                try {
                    MainFrame frame = get();
                    frame.setVisible(true);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    cause.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                            "Failed to start the application:\n" + cause,
                            "Startup Error", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                }
            }
        };
        worker.execute();
    }
}
