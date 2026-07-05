package gui;

import model.*;
import system.RentalSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class EquipmentPanel extends JPanel {

    private final RentalSystem system;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField nameField = new JTextField();
    private final JComboBox<String> categoryBox = new JComboBox<>(
            new String[]{"Electronics", "Media Equipment", "Laboratory Equipment"});
    private final JTextField rateField = new JTextField();

    /** Called after add/edit/delete so other tabs (e.g. Rental's equipment dropdown) can refresh. */
    private Runnable onChange = () -> {};
    public void setOnChange(Runnable onChange) { this.onChange = onChange; }
    private void notifyChanged() { onChange.run(); }

    public EquipmentPanel(RentalSystem system) {
        this.system = system;
        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(28, 32, 28, 32));
        setBackground(UIStyle.CONTENT_BG);

        add(buildHeader(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Equipment ID", "Name", "Category", "Daily Rate (RM)", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        refreshTable();

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(UIStyle.card(new JScrollPane(table)), BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setOpaque(false);
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(buildRowActionBar());
        south.add(Box.createVerticalStrut(12));
        south.add(buildAddForm());
        center.add(south, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
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

    private Equipment selectedEquipment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an equipment row first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return system.getAllEquipment().get(row);
    }

    private void handleEdit() {
        Equipment equipment = selectedEquipment();
        if (equipment == null) return;

        JTextField editName = new JTextField(equipment.getName());
        JTextField editRate = new JTextField(String.valueOf(equipment.getDailyRentalRate()));

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Name:"));
        panel.add(editName);
        panel.add(new JLabel("Daily Rate (RM):"));
        panel.add(editRate);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Equipment: " + equipment.getEquipmentId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String name = editName.getText().trim();
        double rate;
        try {
            rate = Double.parseDouble(editRate.getText().trim());
            if (name.isEmpty() || rate <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please provide a valid name and a positive daily rate.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        equipment.setName(name);
        equipment.setDailyRentalRate(rate);
        system.updateEquipment(equipment);
        refreshTable();
        notifyChanged();
    }

    private void handleDelete() {
        Equipment equipment = selectedEquipment();
        if (equipment == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + equipment.getName() + "\" (" + equipment.getEquipmentId() + ")? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            system.deleteEquipment(equipment);
            refreshTable();
            notifyChanged();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Equipment Management");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);
        JLabel subtitle = new JLabel("Manage rentable items: Electronics, Media, and Laboratory equipment");
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

    private JPanel buildAddForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIStyle.CARD_BG);
        form.setBorder(UIStyle.formCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel formTitle = new JLabel("Add New Equipment");
        formTitle.setFont(UIStyle.FONT_HEADING);
        formTitle.setForeground(UIStyle.TEXT_DARK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 6;
        form.add(formTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0; form.add(UIStyle.fieldLabel("Name"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; form.add(nameField, gbc);

        gbc.gridx = 2; gbc.weightx = 0; form.add(UIStyle.fieldLabel("Category"), gbc);
        gbc.gridx = 3; gbc.weightx = 1; form.add(categoryBox, gbc);

        gbc.gridx = 4; gbc.weightx = 0; form.add(UIStyle.fieldLabel("Rate/day (RM)"), gbc);
        gbc.gridx = 5; gbc.weightx = 1; form.add(rateField, gbc);

        JButton addBtn = new JButton("Add Equipment");
        UIStyle.stylePrimaryButton(addBtn);
        addBtn.addActionListener(e -> handleAdd());

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 6; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(14, 6, 4, 6);
        form.add(addBtn, gbc);

        return form;
    }

    private void handleAdd() {
        String name = nameField.getText().trim();
        String rateText = rateField.getText().trim();
        if (name.isEmpty() || rateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double rate;
        try {
            rate = Double.parseDouble(rateText);
            if (rate <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Daily rate must be a positive number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Equipment equipment = switch (categoryBox.getSelectedItem().toString()) {
            case "Media Equipment" -> new MediaEquipment(name, rate);
            case "Laboratory Equipment" -> new LabEquipment(name, rate);
            default -> new Electronics(name, rate);
        };
        system.addEquipment(equipment);
        refreshTable();
        nameField.setText("");
        rateField.setText("");
        notifyChanged();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        for (Equipment e : system.getAllEquipment()) {
            tableModel.addRow(new Object[]{
                    e.getEquipmentId(), e.getName(), e.getCategory(),
                    String.format("%.2f", e.getDailyRentalRate()),
                    e.isAvailable() ? "Available" : "Rented Out"
            });
        }
    }
}
