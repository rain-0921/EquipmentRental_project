package gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Vertical sidebar with role-aware navigation buttons.
 * The owning frame swaps the right-hand content based on which
 * button the user clicks.
 */
public class SidebarPanel extends JPanel {

    public static final Color HOVER  = new Color(0x2E3D5C);
    public static final Color ACTIVE = Theme.ACCENT;
    public static final Color TEXT   = Color.WHITE;
    public static final Color MUTED  = new Color(0xA9B4D0);

    private final SidebarListener listener;
    private JButton activeButton;
    private JLabel  roleBadge;

    public SidebarPanel(String userName, String userRole, SidebarListener listener) {
        this.listener = listener;
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        setBackground(Theme.PRIMARY);
        setBorder(new EmptyBorder(24, 0, 20, 0));
        setPreferredSize(new java.awt.Dimension(230, 0));

        JLabel brand = new JLabel("Smart Rental");
        brand.setForeground(TEXT);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setBorder(new EmptyBorder(0, 20, 2, 20));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        add(brand);

        JLabel sub = new JLabel("Equipment System");
        sub.setForeground(MUTED);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setBorder(new EmptyBorder(0, 22, 22, 20));
        sub.setAlignmentX(LEFT_ALIGNMENT);
        add(sub);

        roleBadge = new JLabel();
        roleBadge.setOpaque(true);
        roleBadge.setBackground(ACTIVE);
        roleBadge.setForeground(TEXT);
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleBadge.setBorder(new EmptyBorder(8, 18, 8, 18));
        roleBadge.setAlignmentX(LEFT_ALIGNMENT);
        roleBadge.setText(userRole.toUpperCase() + "  -  " + userName);
        roleBadge.setMaximumSize(new java.awt.Dimension(230, 30));
        add(roleBadge);

        add(javax.swing.Box.createVerticalStrut(20));
    }

    /** Add a navigation button. */
    public JButton addNavButton(String label, String key) {
        JButton btn = makeNavButton(label);
        btn.setActionCommand(key);
        btn.addActionListener(e -> {
            setActive(btn);
            listener.onNavigate(key);
        });
        add(btn);
        return btn;
    }

    public void addSeparator() {
        javax.swing.JSeparator sep = new javax.swing.JSeparator();
        sep.setForeground(new Color(0x34406B));
        sep.setBackground(Theme.PRIMARY);
        sep.setMaximumSize(new java.awt.Dimension(200, 2));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        add(javax.swing.Box.createVerticalStrut(14));
        add(sep);
        add(javax.swing.Box.createVerticalStrut(14));
    }

    /** Footer area for actions that don't navigate (e.g. Logout). */
    public JButton addFooterButton(String label, java.awt.event.ActionListener onClick) {
        javax.swing.Box.Filler glue = (javax.swing.Box.Filler)
            javax.swing.Box.createVerticalGlue();
        add(glue);
        JButton btn = makeNavButton(label);
        btn.addActionListener(onClick);
        add(btn);
        return btn;
    }

    public void setActive(JButton btn) {
        if (activeButton != null) {
            activeButton.setBackground(Theme.PRIMARY);
        }
        activeButton = btn;
        if (btn != null) btn.setBackground(ACTIVE);
    }

    public JButton getFirstButton() {
        for (java.awt.Component c : getComponents()) {
            if (c instanceof JButton b) return b;
        }
        return null;
    }

    private JButton makeNavButton(String label) {
        JButton btn = new JButton(label);
        btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btn.setBackground(Theme.PRIMARY);
        btn.setForeground(TEXT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 22));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new java.awt.Dimension(230, 42));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn != activeButton) btn.setBackground(HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn != activeButton) btn.setBackground(Theme.PRIMARY);
            }
        });
        return btn;
    }

    public interface SidebarListener {
        void onNavigate(String key);
    }
}