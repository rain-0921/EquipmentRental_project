package gui;

import model.user.User;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Modal dialog wrapping {@link NewUserPanel}. Pops up when a staff
 * member clicks "+ Create User" in the Users table, validates the
 * form, saves the new user + password hash, and notifies the caller
 * via {@link #getCreatedUser()}.
 */
public class CreateUserDialog extends JDialog {

    private final NewUserPanel form = new NewUserPanel(false);
    private User createdUser;

    public CreateUserDialog(JFrame owner) {
        super(owner, "Create New User", true);
        getContentPane().setBackground(Theme.SURFACE);
        setLayout(new BorderLayout());
        setSize(new Dimension(620, 480));
        setLocationRelativeTo(owner);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(Theme.padded(14));
        JLabel title = new JLabel("Create New User");
        title.setFont(Theme.h2Font());
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);

        JButton saveBtn   = Theme.primaryButton("Create");
        JButton cancelBtn = Theme.ghostButton("Cancel");
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(Theme.SURFACE);
        buttons.setBorder(Theme.padded(12));
        buttons.add(cancelBtn);
        buttons.add(saveBtn);
        add(buttons, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());

        // Make Enter trigger Create from any field (focus can land on
        // any text field after the user tabs through them).
        getRootPane().setDefaultButton(saveBtn);

        // Escape cancels the dialog, matching every other modal.
        javax.swing.KeyStroke esc = javax.swing.KeyStroke.getKeyStroke(
            java.awt.event.KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(esc, "cancel");
        getRootPane().getActionMap().put("cancel",
            new javax.swing.AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    dispose();
                }
            });
    }

    private void onSave() {
        try {
            createdUser = form.saveAndGet();
            GuiUtil.showInfo(this, "User created",
                "Created user " + createdUser.getUserId() + ".");
            dispose();
        } catch (IllegalArgumentException iae) {
            GuiUtil.showError(this, "Cannot create user", iae.getMessage());
        } catch (Exception ex) {
            GuiUtil.showError(this, "Cannot create user",
                "Failed to save:\n" + ex.getMessage());
        }
    }

    /** The user that was just saved, or null if the dialog was
     *  cancelled or the save failed. */
    public User getCreatedUser() {
        return createdUser;
    }
}