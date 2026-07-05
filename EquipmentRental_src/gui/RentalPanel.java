package gui;

import model.*;
import system.RentalSystem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class RentalPanel extends JPanel {

    private final RentalSystem system;
    private final EquipmentPanel equipmentPanel; // to refresh availability there too
    private final BillingPanel billingPanel;      // to push new bills there

    private final JComboBox<Equipment> equipmentBox = new JComboBox<>();
    private final JComboBox<User> userBox = new JComboBox<>();
    private final JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 7, 1));

    private final DefaultTableModel activeModel;
    private final JTable activeTable;

    public RentalPanel(RentalSystem system, EquipmentPanel equipmentPanel, BillingPanel billingPanel) {
        this.system = system;
        this.equipmentPanel = equipmentPanel;
        this.billingPanel = billingPanel;

        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(28, 32, 28, 32));
        setBackground(UIStyle.CONTENT_BG);

        add(buildHeader(), BorderLayout.NORTH);

        activeModel = new DefaultTableModel(
                new Object[]{"Rental ID", "Equipment", "Renter", "Rent Date", "Due Date"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        activeTable = new JTable(activeModel);
        UIStyle.styleTable(activeTable);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(buildRentForm(), BorderLayout.NORTH);
        center.add(UIStyle.card(new JScrollPane(activeTable)), BorderLayout.CENTER);
        center.add(buildReturnBar(), BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        refreshAll();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Rental & Return");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);
        JLabel subtitle = new JLabel("Rent out available equipment and process returns");
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

    private JPanel buildRentForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIStyle.CARD_BG);
        form.setBorder(UIStyle.formCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel formTitle = new JLabel("New Rental");
        formTitle.setFont(UIStyle.FONT_HEADING);
        formTitle.setForeground(UIStyle.TEXT_DARK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 6;
        form.add(formTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0; form.add(UIStyle.fieldLabel("Equipment"), gbc);
        gbc.gridx = 1; gbc.weightx = 1; form.add(equipmentBox, gbc);

        gbc.gridx = 2; gbc.weightx = 0; form.add(UIStyle.fieldLabel("Renter"), gbc);
        gbc.gridx = 3; gbc.weightx = 1; form.add(userBox, gbc);

        gbc.gridx = 4; gbc.weightx = 0; form.add(UIStyle.fieldLabel("Days"), gbc);
        gbc.gridx = 5; gbc.weightx = 1; form.add(daysSpinner, gbc);

        JButton rentBtn = new JButton("Rent Out");
        UIStyle.stylePrimaryButton(rentBtn);
        rentBtn.addActionListener(e -> handleRent());

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 6; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(14, 6, 4, 6);
        form.add(rentBtn, gbc);

        return form;
    }

    private JPanel buildReturnBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bar.setOpaque(false);

        JButton returnBtn = new JButton("Return Selected Item");
        UIStyle.styleSuccessButton(returnBtn);
        returnBtn.addActionListener(e -> handleReturn());

        bar.add(returnBtn);
        return bar;
    }

    private void handleRent() {
        Equipment equipment = (Equipment) equipmentBox.getSelectedItem();
        User user = (User) userBox.getSelectedItem();
        int days = (Integer) daysSpinner.getValue();

        if (equipment == null || user == null) {
            JOptionPane.showMessageDialog(this, "No available equipment or user found.", "Cannot Rent", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            system.createRental(equipment, user, days);
            JOptionPane.showMessageDialog(this,
                    "Rented \"" + equipment.getName() + "\" to " + user.getFullName() + " for " + days + " day(s).",
                    "Rental Created", JOptionPane.INFORMATION_MESSAGE);
            refreshAll();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReturn() {
        int row = activeTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an active rental row first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Rental> active = system.getActiveRentals();
        Rental rental = active.get(row);

        int daysLateInput = 0;
        String lateStr = JOptionPane.showInputDialog(this,
                "How many days late is the return? (0 if on time)", "0");
        if (lateStr == null) return; // cancelled
        try {
            daysLateInput = Integer.parseInt(lateStr.trim());
            if (daysLateInput < 0) daysLateInput = 0;
        } catch (NumberFormatException ignored) { }

        DamageLevel damageLevel = (DamageLevel) JOptionPane.showInputDialog(this,
                "What condition was the equipment returned in?",
                "Condition Check", JOptionPane.QUESTION_MESSAGE, null,
                DamageLevel.values(), DamageLevel.NONE);
        if (damageLevel == null) damageLevel = DamageLevel.NONE; // cancelled = assume no damage

        LocalDate returnDate = rental.getDueDate().plusDays(daysLateInput);
        Bill bill = system.returnRental(rental, returnDate, damageLevel);

        billingPanel.addBill(bill);
        refreshAll();

        JOptionPane.showMessageDialog(this,
                "Return processed. Net payable: RM " + String.format("%.2f", bill.getNetPayable())
                        + "\nFull bill available in the Billing tab.",
                "Return Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshAll() {
        equipmentBox.removeAllItems();
        for (Equipment e : system.getAvailableEquipment()) equipmentBox.addItem(e);

        userBox.removeAllItems();
        for (User u : system.getAllUsers()) userBox.addItem(u);

        activeModel.setRowCount(0);
        for (Rental r : system.getActiveRentals()) {
            activeModel.addRow(new Object[]{
                    r.getRentalId(), r.getEquipment().getName(), r.getUser().getFullName(),
                    r.getRentDate(), r.getDueDate()
            });
        }
        equipmentPanel.refreshTable();
    }

}
