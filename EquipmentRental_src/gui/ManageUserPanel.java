package gui;

import model.*;
import system.RentalSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Manage User tab: lists every user with their Position (role), Name, and ID
 * (e.g. S1001 for students, T1001 for staff), and lets the admin add, edit,
 * or delete users.
 *
 * Student (FYP - final year) users receive a 10% rental discount.
 * Staff users receive a 20% rental discount.
 */
public class ManageUserPanel extends JPanel {

    private final RentalSystem system;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student", "Staff"});
    private final JTextField idPreviewField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JCheckBox finalYearBox = new JCheckBox("Final Year (FYP) - 10% discount");

    /** Called after add/edit/delete so other tabs (e.g. Rental's renter dropdown) can refresh. */
    private Runnable onChange = () -> {};
    public void setOnChange(Runnable onChange) { this.onChange = onChange; }
    private void notifyChanged() { onChange.run(); }

    public ManageUserPanel(RentalSystem system) {
        this.system = system;
        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(28, 32, 28, 32));
        setBackground(UIStyle.CONTENT_BG);

        add(buildHeader(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Position", "Discount"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        refreshTable();

        idPreviewField.setEditable(false);
        idPreviewField.setFocusable(false);
        idPreviewField.setBackground(new Color(241, 242, 246));
        idPreviewField.setForeground(UIStyle.TEXT_MUTED);
        roleBox.addActionListener(e -> refreshIdPreview());
        refreshIdPreview();

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(UIStyle.card(new JScrollPane(table)), BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(buildRowActionBar());
        south.add(Box.createVerticalStrut(16));
        south.add(buildAddForm());
        center.add(south, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Manage User");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);
        JLabel subtitle = new JLabel("Manage students and staff: position, name, and auto-generated ID (never reused, even after delete)");
        subtitle.setFont(UIStyle.FONT_BODY);
        subtitle.setForeground(UIStyle.TEXT_MUTED);

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.add(title);
        textBox.add(Box.createVerticalStrut(4));
        textBox.add(subtitle);

        header.add(textBox, BorderLayout.WEST);
        return header;
    }

    private JPanel buildRowActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bar.setOpaque(false);

        JButton editBtn = new JButton("Edit Selected");
        UIStyle.styleSubtleButton(editBtn);
        editBtn.addActionListener(e -> handleEdit());

        JButton deleteBtn = new JButton("Delete Selected");
        UIStyle.styleDangerButton(deleteBtn);
        deleteBtn.addActionListener(e -> handleDelete());

        bar.add(editBtn);
        bar.add(deleteBtn);
        return bar;
    }

    private JPanel buildAddForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIStyle.CARD_BG);
        form.setBorder(UIStyle.formCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel formTitle = new JLabel("Add New User");
        formTitle.setFont(UIStyle.FONT_HEADING);
        formTitle.setForeground(UIStyle.TEXT_DARK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 6;
        form.add(formTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0; form.add(UIStyle.fieldLabel("Position"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; form.add(roleBox, gbc);

        gbc.gridx = 2; gbc.weightx = 0; form.add(UIStyle.fieldLabel("ID (auto-generated)"), gbc);
        gbc.gridx = 3; gbc.weightx = 1; form.add(idPreviewField, gbc);

        gbc.gridx = 4; gbc.weightx = 0; form.add(UIStyle.fieldLabel("Name"), gbc);
        gbc.gridx = 5; gbc.weightx = 1; form.add(nameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0; gbc.weightx = 0; form.add(UIStyle.fieldLabel("Student Only"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 5; gbc.weightx = 1; form.add(finalYearBox, gbc);
        gbc.gridwidth = 1;

        JButton addBtn = new JButton("Add User");
        UIStyle.stylePrimaryButton(addBtn);
        addBtn.addActionListener(e -> handleAdd());

        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 6; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(14, 6, 4, 6);
        form.add(addBtn, gbc);

        return form;
    }

    private void refreshIdPreview() {
        String role = (String) roleBox.getSelectedItem();
        idPreviewField.setText(system.generateNextUserId(role));
    }

    private void handleAdd() {
        String name = nameField.getText().trim();
        String role = (String) roleBox.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in the Name.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Re-generate right before creating in case another add happened since the last preview refresh.
        String id = system.generateNextUserId(role);

        User user;
        if ("Staff".equals(role)) {
            user = new Staff(id, name);
        } else {
            user = new Student(id, name, finalYearBox.isSelected());
        }

        try {
            system.addUser(user);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Add User", JOptionPane.ERROR_MESSAGE);
            refreshIdPreview();
            return;
        }

        refreshTable();
        nameField.setText("");
        finalYearBox.setSelected(false);
        refreshIdPreview();
        notifyChanged();

        JOptionPane.showMessageDialog(this, "User added with ID: " + id, "User Added", JOptionPane.INFORMATION_MESSAGE);
    }

    private User selectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user row first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return system.getAllUsers().get(row);
    }

    private void handleEdit() {
        User user = selectedUser();
        if (user == null) return;

        JTextField editName = new JTextField(user.getFullName());
        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Name:"));
        panel.add(editName);

        JCheckBox editFinalYear = null;
        if (user instanceof Student student) {
            editFinalYear = new JCheckBox("Final Year (FYP) - 10% discount", student.isFinalYear());
            panel.add(editFinalYear);
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit User: " + user.getUserId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String newName = editName.getText().trim();
        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Mutate the existing object in place (same identity) so any active
        // Rental/Bill objects already referencing this user immediately see
        // the update, instead of pointing at a stale, replaced instance.
        user.setFullName(newName);
        if (user instanceof Student student && editFinalYear != null) {
            student.setFinalYear(editFinalYear.isSelected());
        }

        system.updateUser(user);
        refreshTable();
        notifyChanged();
    }

    private void handleDelete() {
        User user = selectedUser();
        if (user == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + user.getFullName() + "\" (" + user.getUserId() + ")? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            system.deleteUser(user);
            refreshTable();
            notifyChanged();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (User u : system.getAllUsers()) {
            String discount = "-";
            if (u instanceof Staff) {
                discount = "20% (Staff)";
            } else if (u instanceof Student student && student.isFinalYear()) {
                discount = "10% (FYP)";
            }
            tableModel.addRow(new Object[]{ u.getUserId(), u.getFullName(), u.getRole(), discount });
        }
    }

}
