package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** A flat sidebar navigation button with a rounded highlight and a left
 *  accent bar to mark the active section. */
public class SidebarButton extends JButton {

    private boolean active = false;
    private boolean hover = false;

    public SidebarButton(String text) {
        super(text);
        setHorizontalAlignment(SwingConstants.LEFT);
        setFont(UIStyle.FONT_SIDEBAR);
        setForeground(UIStyle.SIDEBAR_TEXT);
        setBorder(new EmptyBorder(12, 20, 12, 16));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? Color.WHITE : UIStyle.SIDEBAR_TEXT);
        setFont(active ? UIStyle.FONT_SIDEBAR.deriveFont(Font.BOLD) : UIStyle.FONT_SIDEBAR);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margin = 10;
        int w = getWidth() - margin * 2;
        int h = getHeight() - 4;

        if (active) {
            g2.setColor(UIStyle.SIDEBAR_BTN_ACTIVE);
            g2.fillRoundRect(margin, 2, w, h, 10, 10);
        } else if (hover) {
            g2.setColor(UIStyle.SIDEBAR_BTN_HOVER);
            g2.fillRoundRect(margin, 2, w, h, 10, 10);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
