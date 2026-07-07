package gui;

import model.user.User;
import service.AuthService;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Login screen. Verifies credentials against the {@code user}
 * table via {@link AuthService} and opens {@link MainShell}
 * on success. Styled to match the sidebar so the transition
 * feels like one app, not two windows.
 */
public class LoginFrame extends javax.swing.JFrame {

    private static final Color BG = Theme.PRIMARY;

    private final JTextField emailField    = new JTextField(20);
    private final JPasswordField pwField   = new JPasswordField(20);
    private final JLabel errorLabel        = new JLabel(" ", SwingConstants.CENTER);
    private final javax.swing.JButton loginButton = Theme.primaryButton("Sign In");
    private final JLabel seedHint          = new JLabel(
        "<html><div style='text-align:center;color:#A9B4D0;font-size:11px;line-height:1.7'>"
      + "<b style='color:#FFFFFF'>Demo accounts</b><br>"
      + "alice.tan@mmu.edu.my / Student@123 (Student)<br>"
      + "bob.lee@mmu.edu.my / Bob@12345 (Student)<br>"
      + "chia.wei@mmu.edu.my / Chia@1234 (Final-Year Student)<br>"
      + "dr.lim@mmu.edu.my / Staff@1234 (Staff)</div></html>",
        SwingConstants.CENTER);

    private final AuthService auth = new AuthService();

    public LoginFrame() {
        super("Sign In - Smart Equipment Rental");
        setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        setUndecorated(false);
        setSize(920, 580);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        add(buildHeroPanel(), BorderLayout.WEST);
        add(buildFormPanel(), BorderLayout.CENTER);

        wireUp();
    }

    private JPanel buildHeroPanel() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(BG);
        hero.setPreferredSize(new Dimension(440, 0));
        hero.setBorder(new EmptyBorder(70, 44, 70, 44));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new javax.swing.BoxLayout(text, javax.swing.BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("Smart Rental");
        brand.setForeground(Color.WHITE);
        brand.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 30));
        brand.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html><div style='color:#A9B4D0;font-size:14px;line-height:1.7'>"
            + "Browse, rent and bill<br>university equipment with one<br>clean workspace."
            + "</div></html>");
        tagline.setAlignmentX(LEFT_ALIGNMENT);
        tagline.setBorder(new EmptyBorder(16, 0, 0, 0));

        text.add(brand);
        text.add(tagline);

        hero.add(text, BorderLayout.NORTH);
        return hero;
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.CARD);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(6, 6, 6, 6);

        JLabel title = new JLabel("Welcome back");
        title.setFont(Theme.titleFont());
        title.setForeground(Theme.TEXT);

        JLabel subtitle = new JLabel("Sign in with your university email.");
        subtitle.setForeground(Theme.MUTED);
        subtitle.setFont(Theme.bodyFont());

        JLabel emailLbl    = fieldLabel("Email");
        JLabel pwLbl       = fieldLabel("Password");

        emailField.setBorder(Theme.fieldBorder());
        emailField.setFont(Theme.bodyFont());
        emailField.setPreferredSize(new Dimension(300, 38));
        pwField.setBorder(Theme.fieldBorder());
        pwField.setFont(Theme.bodyFont());
        pwField.setPreferredSize(new Dimension(300, 38));

        loginButton.setPreferredSize(new Dimension(300, 42));

        errorLabel.setForeground(Theme.DANGER);
        errorLabel.setFont(Theme.smallFont());

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        c.insets = new Insets(0, 6, 6, 6); form.add(title, c);
        c.gridy = 1;
        c.insets = new Insets(0, 6, 24, 6); form.add(subtitle, c);
        c.gridy = 2; c.gridwidth = 1;
        c.insets = new Insets(6, 6, 4, 6); form.add(emailLbl, c);
        c.gridy = 3;
        c.insets = new Insets(0, 6, 14, 6); form.add(emailField, c);
        c.gridy = 4;
        c.insets = new Insets(6, 6, 4, 6); form.add(pwLbl, c);
        c.gridy = 5;
        c.insets = new Insets(0, 6, 12, 6); form.add(pwField, c);
        c.gridy = 6; c.gridx = 0; c.gridwidth = 2;
        c.insets = new Insets(4, 6, 4, 6); form.add(loginButton, c);
        c.gridy = 7;
        c.insets = new Insets(4, 6, 12, 6); form.add(errorLabel, c);
        c.gridy = 8;
        form.add(seedHint, c);

        return form;
    }

    private static JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bodyFont());
        l.setForeground(Theme.MUTED);
        return l;
    }

    private void wireUp() {
        getRootPane().setDefaultButton(loginButton);
        loginButton.addActionListener(e -> attemptLogin());
        emailField.addActionListener(e -> pwField.requestFocusInWindow());
        pwField.addActionListener(e    -> attemptLogin());
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        char[] pw    = pwField.getPassword();
        if (email.isEmpty() || pw.length == 0) {
            errorLabel.setText("Email and password are required.");
            return;
        }
        loginButton.setEnabled(false);
        errorLabel.setText(" ");
        // Don't block the EDT for DB I/O.
        new SwingWorkerAuth(this, email, new String(pw)).execute();
    }

    /** Background worker so login DB call doesn't freeze the UI. */
    private static class SwingWorkerAuth extends javax.swing.SwingWorker<User, Void> {
        private final LoginFrame frame;
        private final String email;
        private final String password;
        private Throwable error;
        SwingWorkerAuth(LoginFrame f, String e, String p) {
            this.frame = f; this.email = e; this.password = p;
        }
        @Override protected User doInBackground() {
            try { return new AuthService().login(email, password).orElse(null); }
            catch (Throwable t) { error = t; return null; }
        }
        @Override protected void done() {
            try {
                User u = get();
                if (u != null) {
                    frame.dispose();
                    new MainShell(u).setVisible(true);
                } else {
                    String msg = error != null ? error.getMessage() : "Invalid email or password.";
                    frame.errorLabel.setText(error != null ? "Login failed: " + msg
                                                          : "Invalid email or password.");
                    frame.loginButton.setEnabled(true);
                }
            } catch (Exception ex) {
                frame.errorLabel.setText("Login failed: " + ex.getMessage());
                frame.loginButton.setEnabled(true);
            }
        }
    }
}