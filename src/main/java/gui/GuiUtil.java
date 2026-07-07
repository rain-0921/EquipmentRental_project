package gui;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * Shared Swing helpers - kept tiny on purpose.
 */
public final class GuiUtil {

    private GuiUtil() {}

    /** Set the system L&amp;F so the app looks native on each OS. */
    public static void installNativeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to cross-platform L&F - already the default.
        }
    }

    public static void showError(java.awt.Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(java.awt.Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static String formatRM(double value) {
        return String.format("RM %.2f", value);
    }
}