package gui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;

/** Static About card. */
public class AboutPanel extends JPanel {

    public AboutPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        setBorder(Theme.padded(Theme.PAD));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = Theme.pageTitle("About this system");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub   = Theme.subtitle("Smart Equipment Rental & Billing System");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 14, 0));
        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        JTextArea body = new JTextArea(
            "University equipment rental management.\n\n"
          + "Roles:\n"
          + "  - Students rent equipment and view their own history.\n"
          + "  - Staff manage the equipment catalogue, users, and\n"
          + "    every rental in the system.\n\n"
          + "Damage tiers (charged on return):\n"
          + "  - None      - no fee\n"
          + "  - Light     - RM 10\n"
          + "  - Moderate  - RM 100\n"
          + "  - Heavy     - RM 1000\n\n"
          + "Built with Java Swing + MySQL."
        );
        body.setEditable(false);
        body.setOpaque(false);
        body.setForeground(Theme.TEXT);
        body.setFont(Theme.bodyFont());
        body.setBorder(Theme.padded(8));

        card.add(body, BorderLayout.CENTER);
        add(card, BorderLayout.NORTH);
    }
}