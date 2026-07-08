package rental.gui;

import rental.model.user.User;
import rental.model.user.UserStatus;
import rental.service.UserService;
import rental.model.user.UserRole;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserAdminPanel extends JPanel {
    private User currentUser;
    private UserService userService;
    private JTable usersTable;
    private DefaultTableModel tableModel;

    public UserAdminPanel(User user) {
        this.currentUser = user;
        this.userService = UserService.getInstance();
        initComponents();
        loadUsers();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("User Administration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topPanel.add(titleLabel, BorderLayout.WEST);

        String[] columns = {"User ID", "Name", "Role", "Status", "Discount Rate"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(tableModel);
        usersTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        usersTable.setRowHeight(28);
        usersTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        usersTable.getTableHeader().setBackground(new Color(233, 236, 239));
        usersTable.setGridColor(new Color(222, 226, 230));
        usersTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        JScrollPane scrollPane = new JScrollPane(usersTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JButton addButton = new JButton("Add User");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setBackground(new Color(40, 167, 69));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setOpaque(true);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddDialog());

        JButton editButton = new JButton("Edit User");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        editButton.setBackground(new Color(255, 193, 7));
        editButton.setForeground(Color.BLACK);
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setOpaque(true);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> showEditDialog());

        JButton inactivateButton = new JButton("Inactivate User");
        inactivateButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        inactivateButton.setBackground(new Color(108, 117, 125));
        inactivateButton.setForeground(Color.WHITE);
        inactivateButton.setFocusPainted(false);
        inactivateButton.setBorderPainted(false);
        inactivateButton.setOpaque(true);
        inactivateButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        inactivateButton.addActionListener(e -> inactivateUser());

        buttonPanel.add(addButton);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(editButton);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(inactivateButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        loadUsers();
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userService.getAllUsers();
        for (User u : users) {
            String discountRate;
            switch (u.getRole()) {
                case STUDENT:
                    discountRate = "0%";
                    break;
                case FINAL_YEAR_STUDENT:
                    discountRate = "15%";
                    break;
                case STAFF:
                    discountRate = "20%";
                    break;
                default:
                    discountRate = "N/A";
            }
            Object[] row = {
                u.getUserId(),
                u.getName(),
                u.getRole().name(),
                u.getStatus() != null ? u.getStatus().name() : UserStatus.ACTIVE.name(),
                discountRate
            };
            tableModel.addRow(row);
        }
    }

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected && value != null) {
                String status = value.toString();
                if (UserStatus.INACTIVE.name().equals(status)) {
                    c.setForeground(new Color(220, 53, 69));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(new Color(40, 167, 69));
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
            } else if (isSelected) {
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Add New User", true);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<UserRole> roleCombo = new JComboBox<>(new UserRole[]{
            UserRole.STUDENT, UserRole.FINAL_YEAR_STUDENT, UserRole.STAFF
        });

        idField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        addLabeledField(panel, "User ID:", idField);
        addLabeledField(panel, "Name:", nameField);
        addLabeledField(panel, "Password:", passField);
        addLabeledField(panel, "Role:", roleCombo);

        JButton saveButton = new JButton("Add User");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setOpaque(true);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setMaximumSize(new Dimension(180, 40));
        saveButton.addActionListener(e -> {
            String result = userService.createUser(
                idField.getText().trim(),
                nameField.getText().trim(),
                new String(passField.getPassword()),
                (UserRole) roleCombo.getSelectedItem()
            );
            if (result.equals("SUCCESS")) {
                dialog.dispose();
                loadUsers();
                JOptionPane.showMessageDialog(this, "User added successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + result);
            }
        });

        panel.add(Box.createVerticalStrut(10));
        panel.add(saveButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit");
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);
        User user = userService.getUser(userId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Edit User", true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField nameField = new JTextField(user.getName());
        JPasswordField passField = new JPasswordField();

        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        passField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JLabel idLabel = new JLabel("User ID: " + userId);
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        idLabel.setForeground(new Color(108, 117, 125));

        JLabel roleLabel = new JLabel("Role: " + user.getRole().name());
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(108, 117, 125));

        addLabeledField(panel, "", idLabel);
        addLabeledField(panel, "", roleLabel);
        addLabeledField(panel, "Name:", nameField);
        addLabeledField(panel, "New Password (leave blank to keep):", passField);

        JButton saveButton = new JButton("Save Changes");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(255, 193, 7));
        saveButton.setForeground(Color.BLACK);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setOpaque(true);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setMaximumSize(new Dimension(180, 40));
        saveButton.addActionListener(e -> {
            String result = userService.updateUser(
                userId,
                nameField.getText().trim(),
                new String(passField.getPassword())
            );
            if (result.equals("SUCCESS")) {
                dialog.dispose();
                loadUsers();
                JOptionPane.showMessageDialog(this, "User updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + result);
            }
        });

        panel.add(Box.createVerticalStrut(10));
        panel.add(saveButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void inactivateUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to inactivate");
            return;
        }

        String userId = (String) tableModel.getValueAt(selectedRow, 0);

        if (userId.equals(currentUser.getUserId())) {
            JOptionPane.showMessageDialog(this, "You cannot inactivate your own account!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to inactivate this user?\n" +
            "User ID: " + userId + "\n\n" +
            "The user will remain in this admin page (with INACTIVE status), " +
            "but will no longer be able to log in to the system.",
            "Confirm Inactivate", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String result = userService.inactivateUser(userId);

        if (result.equals("SUCCESS")) {
            loadUsers();
            JOptionPane.showMessageDialog(this, "User inactivated successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + result);
        }
    }

    private void addLabeledField(JPanel panel, String label, JComponent field) {
        if (!label.isEmpty()) {
            JLabel lbl = new JLabel(label);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lbl.setForeground(new Color(33, 37, 41));
            panel.add(lbl);
            panel.add(Box.createVerticalStrut(5));
        }
        if (field instanceof JTextField) {
            ((JTextField) field).setFont(new Font("Segoe UI", Font.PLAIN, 14));
            ((JTextField) field).setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            panel.add((JTextField) field);
        } else if (field instanceof JPasswordField) {
            ((JPasswordField) field).setFont(new Font("Segoe UI", Font.PLAIN, 14));
            ((JPasswordField) field).setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            panel.add((JPasswordField) field);
        } else if (field instanceof JComboBox) {
            ((JComboBox<?>) field).setFont(new Font("Segoe UI", Font.PLAIN, 14));
            ((JComboBox<?>) field).setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            panel.add((JComboBox<?>) field);
        } else if (field instanceof JLabel) {
            panel.add((JLabel) field);
        }
        panel.add(Box.createVerticalStrut(12));
    }
}
