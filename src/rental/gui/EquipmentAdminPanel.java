package rental.gui;

import rental.model.user.User;
import rental.service.EquipmentService;
import rental.model.equipment.Equipment;
import rental.model.equipment.EquipmentStatus;
import rental.model.equipment.EquipmentCategory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EquipmentAdminPanel extends JPanel {
    private User currentUser;
    private EquipmentService equipmentService;
    private JTable equipmentTable;
    private DefaultTableModel tableModel;

    public EquipmentAdminPanel(User user) {
        this.currentUser = user;
        this.equipmentService = EquipmentService.getInstance();
        initComponents();
        loadEquipment();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("Equipment Administration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topPanel.add(titleLabel, BorderLayout.WEST);

        String[] columns = {"ID", "Name", "Category", "Status", "Daily Rate"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        equipmentTable = new JTable(tableModel);
        equipmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        equipmentTable.setRowHeight(28);
        equipmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        equipmentTable.getTableHeader().setBackground(new Color(233, 236, 239));
        equipmentTable.setGridColor(new Color(222, 226, 230));
        JScrollPane scrollPane = new JScrollPane(equipmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JButton addButton = new JButton("Add Equipment");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setBackground(new Color(40, 167, 69));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setOpaque(true);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddDialog());

        JButton editButton = new JButton("Edit Equipment");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        editButton.setBackground(new Color(255, 193, 7));
        editButton.setForeground(Color.BLACK);
        editButton.setFocusPainted(false);
        editButton.setBorderPainted(false);
        editButton.setOpaque(true);
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> showEditDialog());

        JButton deleteButton = new JButton("Delete Equipment");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.setBorderPainted(false);
        deleteButton.setOpaque(true);
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> deleteEquipment());

        JButton repairButton = new JButton("Mark as Repaired");
        repairButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        repairButton.setBackground(new Color(0, 123, 255));
        repairButton.setForeground(Color.WHITE);
        repairButton.setFocusPainted(false);
        repairButton.setBorderPainted(false);
        repairButton.setOpaque(true);
        repairButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        repairButton.addActionListener(e -> markAsRepaired());

        buttonPanel.add(addButton);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(editButton);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(deleteButton);
        buttonPanel.add(Box.createHorizontalStrut(8));
        buttonPanel.add(repairButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        loadEquipment();
    }

    private void loadEquipment() {
        tableModel.setRowCount(0);
        List<Equipment> equipmentList = equipmentService.getAllEquipment();
        for (Equipment eq : equipmentList) {
            Object[] row = {
                eq.getEquipmentId(),
                eq.getName(),
                eq.getCategory().name(),
                eq.getStatus().name(),
                String.format("RM %.2f", eq.getDailyRate())
            };
            tableModel.addRow(row);
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Add New Equipment", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JComboBox<EquipmentCategory> categoryCombo = new JComboBox<>(EquipmentCategory.values());
        JTextField rateField = new JTextField();

        idField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        descField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        rateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        addLabeledField(panel, "Equipment ID:", idField);
        addLabeledField(panel, "Name:", nameField);
        addLabeledField(panel, "Description:", descField);
        addLabeledField(panel, "Category:", categoryCombo);
        addLabeledField(panel, "Daily Rate (RM):", rateField);

        JButton saveButton = new JButton("Add Equipment");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setOpaque(true);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setMaximumSize(new Dimension(200, 40));
        saveButton.addActionListener(e -> {
            try {
                String result = equipmentService.createEquipment(
                    idField.getText().trim(),
                    nameField.getText().trim(),
                    descField.getText().trim(),
                    (EquipmentCategory) categoryCombo.getSelectedItem(),
                    Double.parseDouble(rateField.getText().trim())
                );
                if (result.equals("SUCCESS")) {
                    dialog.dispose();
                    loadEquipment();
                    JOptionPane.showMessageDialog(this, "Equipment added successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + result);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid daily rate");
            }
        });

        panel.add(Box.createVerticalStrut(10));
        panel.add(saveButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showEditDialog() {
        int selectedRow = equipmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select equipment to edit");
            return;
        }

        String equipmentId = (String) tableModel.getValueAt(selectedRow, 0);
        Equipment eq = equipmentService.getEquipment(equipmentId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            "Edit Equipment", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JTextField nameField = new JTextField(eq.getName());
        JTextField descField = new JTextField(eq.getDescription());
        JComboBox<EquipmentCategory> categoryCombo = new JComboBox<>(EquipmentCategory.values());
        categoryCombo.setSelectedItem(eq.getCategory());
        JTextField rateField = new JTextField(String.valueOf(eq.getDailyRate()));

        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        descField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        rateField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JLabel idLabel = new JLabel("Equipment ID: " + equipmentId);
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        idLabel.setForeground(new Color(108, 117, 125));

        addLabeledField(panel, "", idLabel);
        addLabeledField(panel, "Name:", nameField);
        addLabeledField(panel, "Description:", descField);
        addLabeledField(panel, "Category:", categoryCombo);
        addLabeledField(panel, "Daily Rate (RM):", rateField);

        JButton saveButton = new JButton("Save Changes");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(255, 193, 7));
        saveButton.setForeground(Color.BLACK);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setOpaque(true);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setMaximumSize(new Dimension(200, 40));
        saveButton.addActionListener(e -> {
            try {
                String result = equipmentService.updateEquipment(
                    equipmentId,
                    nameField.getText().trim(),
                    descField.getText().trim(),
                    (EquipmentCategory) categoryCombo.getSelectedItem(),
                    Double.parseDouble(rateField.getText().trim())
                );
                if (result.equals("SUCCESS")) {
                    dialog.dispose();
                    loadEquipment();
                    JOptionPane.showMessageDialog(this, "Equipment updated successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Error: " + result);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid daily rate");
            }
        });

        panel.add(Box.createVerticalStrut(10));
        panel.add(saveButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteEquipment() {
        int selectedRow = equipmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select equipment to delete");
            return;
        }

        String equipmentId = (String) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this equipment?\nEquipment ID: " + equipmentId,
            "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String result = equipmentService.deleteEquipment(equipmentId);
        
        if (result.equals("SUCCESS")) {
            loadEquipment();
            JOptionPane.showMessageDialog(this, "Equipment deleted successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + result);
        }
    }

    private void markAsRepaired() {
        int selectedRow = equipmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select equipment to mark as repaired");
            return;
        }

        String equipmentId = (String) tableModel.getValueAt(selectedRow, 0);
        String result = equipmentService.markAsRepaired(equipmentId);
        
        if (result.equals("SUCCESS")) {
            loadEquipment();
            JOptionPane.showMessageDialog(this, "Equipment marked as repaired!");
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
