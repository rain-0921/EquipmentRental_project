package gui;

import model.user.User;
import model.user.UserFactory;
import model.user.UserType;
import repository.UserRepository;
import security.PasswordHasher;

import db.DatabaseManager;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * Form panel for capturing a brand-new user (student or staff).
 * Used in two places:
 *
 *   1. As the body of {@link CreateUserDialog} (modal "Create" flow).
 *   2. As the standalone "New User" tab in the staff shell - in that
 *      mode the panel renders its own Create button.
 *
 * Either way the form fields, validation, and persistence are the
 * same; the difference is purely whether the panel shows a submit
 * button on its own surface.
 */
public class NewUserPanel extends JPanel {

    private static final double FINAL_YEAR_STUDENT_DISCOUNT = 0.15;
    private static final double OTHER_STUDENT_DISCOUNT      = 0.00;
    private static final double STAFF_DISCOUNT              = 0.20;

    private final JTextField idField       = new JTextField(16);
    private final JTextField nameField     = new JTextField(20);
    private final JTextField emailField    = new JTextField(20);
    private final JComboBox<UserType> typeBox =
        new JComboBox<>(new UserType[] { UserType.STUDENT, UserType.STAFF });
    private final JCheckBox   finalYearBox = new JCheckBox("Final-year student (15% discount)");
    private final JTextField discountField = new JTextField("0.20", 6);
    private final JPasswordField passwordField = new JPasswordField(20);

    private final UserRepository repo = new UserRepository();
    /** Optional success hook - fired with the newly created user. */
    private java.util.function.Consumer<User> onCreated;

    public NewUserPanel() {
        this(true);
    }

    /** @param showSubmitButton when true, the panel renders its own
     *                          "Create User" button. Set false when
     *                          embedding in {@link CreateUserDialog}. */
    public NewUserPanel(boolean showSubmitButton) {
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        setBorder(Theme.padded(Theme.PAD));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = Theme.pageTitle("Create a new user");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub   = Theme.subtitle("Fill in the user's details. The initial password is set here and can be changed later.");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 16, 0));
        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(Theme.padded(8));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        idField.setBorder(Theme.fieldBorder());
        idField.setPreferredSize(new java.awt.Dimension(280, 34));
        nameField.setBorder(Theme.fieldBorder());
        nameField.setPreferredSize(new java.awt.Dimension(280, 34));
        emailField.setBorder(Theme.fieldBorder());
        emailField.setPreferredSize(new java.awt.Dimension(280, 34));
        typeBox.setBackground(Theme.CARD);
        typeBox.setFont(Theme.bodyFont());
        typeBox.setBorder(Theme.fieldBorder());
        typeBox.setPreferredSize(new java.awt.Dimension(200, 34));
        discountField.setBorder(Theme.fieldBorder());
        discountField.setPreferredSize(new java.awt.Dimension(80, 34));
        passwordField.setBorder(Theme.fieldBorder());
        passwordField.setPreferredSize(new java.awt.Dimension(280, 34));
        finalYearBox.setOpaque(false);
        finalYearBox.setFont(Theme.bodyFont());
        finalYearBox.setForeground(Theme.TEXT);

        int row = 0;
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        form.add(label("User ID"), c);
        c.gridx = 1; c.weightx = 1.0;
        form.add(idField, c);

        c.gridx = 0; c.gridy = ++row; c.weightx = 0;
        form.add(label("Full Name"), c);
        c.gridx = 1; c.weightx = 1.0;
        form.add(nameField, c);

        c.gridx = 0; c.gridy = ++row; c.weightx = 0;
        form.add(label("Email"), c);
        c.gridx = 1; c.weightx = 1.0;
        form.add(emailField, c);

        c.gridx = 0; c.gridy = ++row; c.weightx = 0;
        form.add(label("User Type"), c);
        c.gridx = 1; c.weightx = 1.0;
        form.add(typeBox, c);

        c.gridx = 0; c.gridy = ++row; c.weightx = 0;
        form.add(label("Final Year?"), c);
        c.gridx = 1; c.weightx = 1.0;
        form.add(finalYearBox, c);

        c.gridx = 0; c.gridy = ++row; c.weightx = 0;
        form.add(label("Discount Rate (0.0-1.0)"), c);
        c.gridx = 1; c.weightx = 1.0;
        form.add(discountField, c);

        c.gridx = 0; c.gridy = ++row; c.weightx = 0;
        form.add(label("Initial Password"), c);
        c.gridx = 1; c.weightx = 1.0;
        form.add(passwordField, c);

        card.add(form, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        if (showSubmitButton) {
            javax.swing.JButton clearBtn = Theme.ghostButton("Clear");
            javax.swing.JButton createBtn = Theme.primaryButton("Create User");
            createBtn.setName("createUserBtn");
            footer.add(clearBtn);
            footer.add(createBtn);
            clearBtn.addActionListener(e -> clear());
            createBtn.addActionListener(e -> onStandaloneCreateClicked(createBtn));
        }
        card.add(footer, BorderLayout.SOUTH);

        add(card, BorderLayout.NORTH);

        // Keep the discount field in sync with the type + final-year
        // selection so staff don't have to memorise the policy.
        typeBox.addActionListener(e -> syncDiscountWithDefaults());
        finalYearBox.addActionListener(e -> syncDiscountWithDefaults());
        syncDiscountWithDefaults();

        // Default focus to the first empty field.
        javax.swing.SwingUtilities.invokeLater(idField::requestFocusInWindow);
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bodyFont());
        l.setForeground(Theme.MUTED);
        return l;
    }

    /** Wire a callback fired after a successful save. */
    public void setOnCreated(java.util.function.Consumer<User> cb) {
        this.onCreated = cb;
    }

    /** Auto-populate the discount field with the canonical rate for
     *  the selected user type. The staff can still override it. */
    private void syncDiscountWithDefaults() {
        UserType t = (UserType) typeBox.getSelectedItem();
        if (t == UserType.STAFF) {
            discountField.setText(String.valueOf(STAFF_DISCOUNT));
            finalYearBox.setEnabled(false);
            finalYearBox.setSelected(false);
        } else {
            finalYearBox.setEnabled(true);
            discountField.setText(String.valueOf(
                finalYearBox.isSelected() ? FINAL_YEAR_STUDENT_DISCOUNT
                                          : OTHER_STUDENT_DISCOUNT));
        }
    }

    /** Reset every field to empty/default. */
    public void clear() {
        idField.setText("");
        nameField.setText("");
        emailField.setText("");
        typeBox.setSelectedIndex(0);
        finalYearBox.setSelected(false);
        discountField.setText(String.valueOf(OTHER_STUDENT_DISCOUNT));
        passwordField.setText("");
        syncDiscountWithDefaults();
    }

    /**
     * Validate the form, persist the user, and return the saved
     * {@link User}. Throws an {@link IllegalArgumentException} with
     * a user-friendly message if validation fails.
     */
    public User saveAndGet() throws Exception {
        String id      = idField.getText().trim();
        String name    = nameField.getText().trim();
        String email   = emailField.getText().trim();
        UserType type  = (UserType) typeBox.getSelectedItem();
        boolean fy     = finalYearBox.isSelected() && type == UserType.STUDENT;
        String pw      = new String(passwordField.getPassword());

        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || pw.isEmpty()) {
            throw new IllegalArgumentException("User ID, name, email and initial password are required.");
        }
        if (pw.length() < 6) {
            throw new IllegalArgumentException("Initial password must be at least 6 characters.");
        }
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        double discount;
        try {
            discount = Double.parseDouble(discountField.getText().trim());
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Discount rate must be a number.");
        }
        if (discount < 0.0 || discount > 1.0) {
            throw new IllegalArgumentException("Discount rate must be between 0.0 and 1.0.");
        }

        List<User> existing = repo.findAll();
        for (User u : existing) {
            if (u.getUserId().equalsIgnoreCase(id)) {
                throw new IllegalArgumentException("User ID already exists: " + id);
            }
        }

        User u = UserFactory.create(id, name, email, type, discount, fy, null);
        User saved = repo.insert(u);
        // Seed the password hash alongside the user record.
        String hash = PasswordHasher.hash(pw);
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE user SET password_hash = ? WHERE user_id = ?")) {
            ps.setString(1, hash);
            ps.setString(2, id);
            ps.executeUpdate();
        }
        return saved;
    }

    /** Handler for the standalone Create button. Reuses the same
     *  saveAndGet path and shows a success dialog. */
    private void onStandaloneCreateClicked(javax.swing.JButton source) {
        source.setEnabled(false);
        try {
            User created = saveAndGet();
            GuiUtil.showInfo(this, "User created",
                String.format("Created user %s (%s).%nThey can now sign in with the initial password you set.",
                    created.getUserId(), created.getFullName()));
            if (onCreated != null) {
                try { onCreated.accept(created); } catch (Exception ignored) {}
            }
            clear();
        } catch (IllegalArgumentException iae) {
            GuiUtil.showError(this, "Cannot create user", iae.getMessage());
        } catch (Exception ex) {
            GuiUtil.showError(this, "Cannot create user",
                "Failed to save:\n" + ex.getMessage());
        } finally {
            source.setEnabled(true);
            javax.swing.SwingUtilities.invokeLater(idField::requestFocusInWindow);
        }
    }
}