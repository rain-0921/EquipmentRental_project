package gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Centralised colours, fonts, and small shared UI helpers so every panel
 * looks consistent. Keeping this in one place means the whole app's look
 * can be re-themed by editing values here only.
 */
public final class UIStyle {

    // ---- Sidebar ----
    public static final Color SIDEBAR_BG         = new Color(24, 27, 37);
    public static final Color SIDEBAR_BTN_HOVER  = new Color(37, 41, 56);
    public static final Color SIDEBAR_BTN_ACTIVE = new Color(79, 70, 229);   // indigo accent
    public static final Color SIDEBAR_TEXT       = new Color(199, 203, 214);
    public static final Color SIDEBAR_TEXT_MUTED = new Color(122, 128, 145);
    public static final Color SIDEBAR_DIVIDER    = new Color(41, 45, 60);

    // ---- Content ----
    public static final Color CONTENT_BG   = new Color(246, 247, 251);
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color CARD_SHADOW  = new Color(15, 23, 42, 18);

    public static final Color ACCENT       = new Color(79, 70, 229);   // indigo
    public static final Color ACCENT_DARK  = new Color(63, 56, 189);
    public static final Color ACCENT_SOFT  = new Color(238, 236, 253);
    public static final Color SUCCESS      = new Color(22, 163, 116);
    public static final Color DANGER       = new Color(220, 68, 68);
    public static final Color DANGER_SOFT  = new Color(254, 236, 236);

    public static final Color TEXT_DARK    = new Color(24, 27, 37);
    public static final Color TEXT_MUTED   = new Color(107, 114, 133);
    public static final Color BORDER       = new Color(228, 230, 236);

    // ---- Typography ----
    // Falls back gracefully if "Segoe UI" isn't installed on the host OS.
    private static final String FAMILY = resolveFontFamily();

    public static final Font FONT_TITLE     = new Font(FAMILY, Font.BOLD, 21);
    public static final Font FONT_HEADING   = new Font(FAMILY, Font.BOLD, 15);
    public static final Font FONT_BODY      = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font(FAMILY, Font.BOLD, 13);
    public static final Font FONT_SIDEBAR   = new Font(FAMILY, Font.PLAIN, 14);
    public static final Font FONT_MONO      = new Font("Consolas", Font.PLAIN, 13);

    private static String resolveFontFamily() {
        String[] preferred = {"Segoe UI", "Helvetica Neue", "Ubuntu", "Arial"};
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String want : preferred) {
            for (String have : available) {
                if (have.equalsIgnoreCase(want)) return want;
            }
        }
        return Font.SANS_SERIF;
    }

    // ---- Shared component helpers ----

    /** Wraps content in a white card with a rounded border and a soft drop shadow. */
    public static JPanel card(Component inner) {
        JPanel shadowWrap = new JPanel(new BorderLayout());
        shadowWrap.setOpaque(false);
        shadowWrap.setBorder(new EmptyBorder(0, 0, 3, 3));

        RoundedPanel card = new RoundedPanel(12, CARD_BG, BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(4, 4, 4, 4));
        card.add(inner, BorderLayout.CENTER);

        shadowWrap.add(card, BorderLayout.CENTER);
        return shadowWrap;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_DARK);
        table.setSelectionBackground(ACCENT_SOFT);
        table.setSelectionForeground(TEXT_DARK);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setFont(FONT_BODY_BOLD);
        table.getTableHeader().setBackground(new Color(250, 250, 253));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
    }

    public static void styleButton(JButton btn, Color bg, Color fg) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(FONT_BODY_BOLD);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
    }

    public static void stylePrimaryButton(JButton btn) { styleButton(btn, ACCENT, Color.WHITE); }
    public static void styleSuccessButton(JButton btn) { styleButton(btn, SUCCESS, Color.WHITE); }
    public static void styleDangerButton(JButton btn)  { styleButton(btn, DANGER, Color.WHITE); }
    public static void styleSubtleButton(JButton btn)  { styleButton(btn, new Color(241, 242, 246), TEXT_DARK); }

    public static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY_BOLD);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    public static Border formCardBorder() {
        return BorderFactory.createCompoundBorder(
                new RoundedLineBorder(12, BORDER),
                new EmptyBorder(18, 20, 18, 20));
    }

    private UIStyle() {}

    /** A simple flat panel painted with rounded corners and a thin border. */
    public static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color fill;
        private final Color edge;

        public RoundedPanel(int radius, Color fill, Color edge) {
            this.radius = radius;
            this.fill = fill;
            this.edge = edge;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(edge);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** A rounded-rectangle line border usable anywhere a Border is expected. */
    public static class RoundedLineBorder implements Border {
        private final int radius;
        private final Color color;

        public RoundedLineBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
        @Override public boolean isBorderOpaque() { return false; }
    }
}
