package gui;

import model.user.User;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Read-only view of the signed-in user's account details. */
public class ProfilePanel extends JPanel {

    public ProfilePanel(User user) {
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        setBorder(Theme.padded(Theme.PAD));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = Theme.pageTitle("My Profile");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub   = Theme.subtitle("Your account details and discount rate.");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 14, 0));
        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(Theme.padded(8));

        JLabel name    = row(user.getFullName(), true);
        JLabel id      = row("User ID: " + user.getUserId(), false);
        JLabel email   = row("Email: " + user.getEmail(), false);
        JLabel role    = row("Role: " + user.getType().getDisplayName(), false);
        JLabel disc    = row(String.format("Discount: %.1f%%", user.getDiscountRate() * 100), false);
        JLabel notes   = row(user.isFinalYear()
            ? "Notes: Final-year student"
            : "Notes: ", false);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(8, 8, 8, 8);
        c.fill   = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(name, c);
        c.gridy = 1; form.add(id, c);
        c.gridy = 2; form.add(email, c);
        c.gridy = 3; form.add(role, c);
        c.gridy = 4; form.add(disc, c);
        c.gridy = 5; form.add(notes, c);

        card.add(form, BorderLayout.CENTER);
        add(card, BorderLayout.NORTH);
    }

    private static JLabel row(String text, boolean heading) {
        JLabel l = new JLabel(text);
        l.setFont(heading ? Theme.h2Font() : Theme.bodyFont());
        l.setForeground(heading ? Theme.TEXT : Theme.MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        return l;
    }
}