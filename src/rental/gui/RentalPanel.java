package rental.gui;

import rental.model.user.User;
import rental.service.RentalService;
import rental.model.equipment.Equipment;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RentalPanel extends JPanel {
    private User currentUser;
    private RentalService rentalService;
    private JTextField equipmentIdField;
    private JTextField daysField;
    private JComboBox<String> equipmentComboBox;
    private JTextArea resultArea;

    public RentalPanel(User user) {
        this.currentUser = user;
        this.rentalService = RentalService.getInstance();
        initComponents();
        loadAvailableEquipment();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("New Equipment Rental");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(25, 35, 25, 35)
        ));

        JLabel selectLabel = new JLabel("Select Equipment");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectLabel.setForeground(new Color(33, 37, 41));
        selectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        equipmentComboBox = new JComboBox<>();
        equipmentComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        equipmentComboBox.setMaximumSize(new Dimension(350, 35));
        equipmentComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel orLabel = new JLabel("Or enter Equipment ID manually:");
        orLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        orLabel.setForeground(new Color(108, 117, 125));
        orLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        equipmentIdField = new JTextField();
        equipmentIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        equipmentIdField.setMaximumSize(new Dimension(350, 35));
        equipmentIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        equipmentIdField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel daysLabel = new JLabel("Rental Duration (days)");
        daysLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        daysLabel.setForeground(new Color(33, 37, 41));
        daysLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        daysField = new JTextField();
        daysField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        daysField.setMaximumSize(new Dimension(350, 35));
        daysField.setAlignmentX(Component.LEFT_ALIGNMENT);
        daysField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton rentButton = new JButton("Rent Equipment");
        rentButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rentButton.setBackground(new Color(40, 167, 69));
        rentButton.setForeground(Color.WHITE);
        rentButton.setFocusPainted(false);
        rentButton.setBorderPainted(false);
        rentButton.setOpaque(true);
        rentButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        rentButton.setMaximumSize(new Dimension(220, 42));
        rentButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rentButton.addActionListener(e -> performRental());

        resultArea = new JTextArea(6, 40);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setBackground(new Color(248, 249, 250));
        resultArea.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        JScrollPane resultScroll = new JScrollPane(resultArea);

        formPanel.add(selectLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(equipmentComboBox);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(orLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(equipmentIdField);
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(daysLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(daysField);
        formPanel.add(Box.createVerticalStrut(25));
        formPanel.add(rentButton);
        formPanel.add(Box.createVerticalStrut(25));
        
        JLabel resultTitle = new JLabel("Result");
        resultTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resultTitle.setForeground(new Color(33, 37, 41));
        resultTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(resultTitle);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(resultScroll);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }

    public void refreshData() {
        loadAvailableEquipment();
        resultArea.setText("");
        equipmentIdField.setText("");
        daysField.setText("");
    }

    private void loadAvailableEquipment() {
        equipmentComboBox.removeAllItems();
        List<Equipment> available = rentalService.getAvailableEquipment();
        for (Equipment eq : available) {
            String item = eq.getEquipmentId() + " - " + eq.getName() + 
                         " (" + eq.getCategory().name() + ")";
            equipmentComboBox.addItem(item);
        }
    }

    private void performRental() {
        String equipmentId = equipmentIdField.getText().trim();
        if (equipmentId.isEmpty()) {
            String selected = (String) equipmentComboBox.getSelectedItem();
            if (selected != null) {
                equipmentId = selected.split(" - ")[0];
            }
        }

        String daysText = daysField.getText().trim();

        if (equipmentId.isEmpty()) {
            resultArea.setText("Error: Please select or enter an Equipment ID");
            return;
        }

        if (daysText.isEmpty()) {
            resultArea.setText("Error: Please enter rental duration");
            return;
        }

        int days;
        try {
            days = Integer.parseInt(daysText);
        } catch (NumberFormatException ex) {
            resultArea.setText("Error: Rental days must be a positive integer");
            return;
        }

        if (days <= 0) {
            resultArea.setText("Error: Rental days must be a positive integer");
            return;
        }

        String result = rentalService.rentEquipment(equipmentId, currentUser, days);
        
        if (result.startsWith("SUCCESS:")) {
            String rentalId = result.substring(8);
            resultArea.setText("Rental Successful!\n\nRental ID: " + rentalId + 
                             "\nEquipment: " + equipmentId + 
                             "\nDuration: " + days + " days\n\n" +
                             "Please remember your Rental ID for return submission.");
            loadAvailableEquipment();
            equipmentIdField.setText("");
            daysField.setText("");
        } else {
            resultArea.setText("Rental Failed!\n\nReason: " + result);
        }
    }
}
