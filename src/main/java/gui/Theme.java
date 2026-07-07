package gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Centralised visual constants and small helpers. Every screen pulls
 * its colors, fonts, and button styling from here so the whole app
 * shares one coherent look.
 */
public final class Theme {

    // Core palette
    public static final Color PRIMARY      = new Color(0x1F2A44);
    public static final Color PRIMARY_DARK = new Color(0x161E33);
    public static final Color ACCENT       = new Color(0x3D5AFE);
    public static final Color ACCENT_DARK  = new Color(0x2E47D9);
    public static final Color ACCENT_SOFT  = new Color(0xE8ECFF);
    public static final Color SURFACE      = new Color(0xF5F7FB);
    public static final Color CARD         = Color.WHITE;
    public static final Color BORDER       = new Color(0xE2E6EF);
    public static final Color TEXT         = new Color(0x1F2937);
    public static final Color MUTED        = new Color(0x6B7280);

    // Semantic
    public static final Color SUCCESS      = new Color(0x10B981);
    public static final Color SUCCESS_DARK = new Color(0x059669);
    public static final Color WARN         = new Color(0xF59E0B);
    public static final Color WARN_DARK    = new Color(0xD97706);
    public static final Color DANGER       = new Color(0xEF4444);
    public static final Color DANGER_DARK  = new Color(0xDC2626);
    public static final Color DISABLED_BG  = new Color(0xC4C9D4);

    // Sizing
    public static final int   RADIUS       = 10;
    public static final int   PAD          = 16;

    // Fonts (lazy-initialized so the L&F is settled by the time we render)
    public static Font titleFont()  { return new Font("Segoe UI", Font.BOLD,  22).deriveFont(22f); }
    public static Font h2Font()     { return new Font("Segoe UI", Font.BOLD,  16).deriveFont(16f); }
    public static Font bodyFont()   { return new Font("Segoe UI", Font.PLAIN, 13).deriveFont(13f); }
    public static Font smallFont()  { return new Font("Segoe UI", Font.PLAIN, 11).deriveFont(11f); }
    public static Font buttonFont() { return new Font("Segoe UI", Font.BOLD,  13).deriveFont(13f); }

    private Theme() {}

    /** Solid filled primary action. */
    public static JButton primaryButton(String text) {
        return makeButton(text, ACCENT, ACCENT_DARK, Color.WHITE);
    }

    /** Solid filled success action (e.g. confirm, save). */
    public static JButton successButton(String text) {
        return makeButton(text, SUCCESS, SUCCESS_DARK, Color.WHITE);
    }

    /** Solid filled danger action. */
    public static JButton dangerButton(String text) {
        return makeButton(text, DANGER, DANGER_DARK, Color.WHITE);
    }

    /** Neutral secondary action. */
    public static JButton ghostButton(String text) {
        return makeButton(text, SURFACE, BORDER, TEXT);
    }

    /** Section title. */
    public static JLabel sectionTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(h2Font());
        l.setForeground(TEXT);
        return l;
    }

    /** Page title - larger than {@link #sectionTitle(String)}. */
    public static JLabel pageTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(titleFont());
        l.setForeground(TEXT);
        return l;
    }

    /** Soft, slightly muted helper text under a title. */
    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(bodyFont());
        l.setForeground(MUTED);
        return l;
    }

    /** Empty padding border. */
    public static Border padded(int pad) {
        return BorderFactory.createEmptyBorder(pad, pad, pad, pad);
    }

    /** Card-style border: 1px line + soft padding. */
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
    }

    /** Field-style border for text inputs. */
    public static Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    // -- internals ----------------------------------------------------

    private static JButton makeButton(String text, Color base, Color hover, Color fg) {
        JButton btn = new JButton(text) {
            private boolean hovering = false;
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = !isEnabled() ? DISABLED_BG
                            : getModel().isPressed() ? darker(base)
                            : hovering            ? hover
                            :                       base;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth()  - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
            @Override public boolean isContentAreaFilled() { return false; }
        };
        btn.setForeground(fg);
        btn.setFont(buttonFont());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.repaint(); }
            @Override public void mouseExited (MouseEvent e) { btn.repaint(); }
        });
        return btn;
    }

    private static Color darker(Color c) {
        return new Color(Math.max(0, c.getRed()   - 20),
                        Math.max(0, c.getGreen() - 20),
                        Math.max(0, c.getBlue()  - 20));
    }
}