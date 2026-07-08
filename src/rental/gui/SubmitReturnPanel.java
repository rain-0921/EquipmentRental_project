package rental.gui;

import rental.model.user.User;
import rental.service.RentalService;
import rental.model.rental.Rental;
import rental.model.rental.RentalStatus;
import rental.model.penalty.DamageSeverity;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SubmitReturnPanel extends JPanel {
    private User currentUser;
    private RentalService rentalService;
    private JComboBox<String> rentalComboBox;
    private JComboBox<DamageSeverity> severityComboBox;
    private JTextArea resultArea;
    private JLabel lateDaysLabel;

    public SubmitReturnPanel(User user) {
        this.currentUser = user;
        this.rentalService = RentalService.getInstance();
        initComponents();
        loadEligibleRentals();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("Submit Equipment Return");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            BorderFactory.createEmptyBorder(25, 35, 25, 35)
        ));

        JLabel selectLabel = new JLabel("Select Rental");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        selectLabel.setForeground(new Color(33, 37, 41));
        selectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rentalComboBox = new JComboBox<>();
        rentalComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        rentalComboBox.setMaximumSize(new Dimension(400, 35));
        rentalComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        rentalComboBox.addActionListener(e -> updateLateDaysInfo());

        lateDaysLabel = new JLabel("");
        lateDaysLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lateDaysLabel.setForeground(new Color(220, 53, 69));
        lateDaysLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel severityLabel = new JLabel("Damage Severity");
        severityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        severityLabel.setForeground(new Color(33, 37, 41));
        severityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel severityHint = new JLabel("Report any damage to the equipment (if any)");
        severityHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        severityHint.setForeground(new Color(108, 117, 125));
        severityHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        severityComboBox = new JComboBox<>(DamageSeverity.values());
        severityComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        severityComboBox.setMaximumSize(new Dimension(220, 35));
        severityComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton submitButton = new JButton("Submit Return Request");
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        submitButton.setBackground(new Color(0, 123, 255));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setOpaque(true);
        submitButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitButton.setMaximumSize(new Dimension(250, 42));
        submitButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitButton.addActionListener(e -> submitReturn());

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
        formPanel.add(rentalComboBox);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(lateDaysLabel);
        formPanel.add(Box.createVerticalStrut(18));
        formPanel.add(severityLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(severityHint);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(severityComboBox);
        formPanel.add(Box.createVerticalStrut(25));
        formPanel.add(submitButton);
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
        loadEligibleRentals();
        resultArea.setText("");
        lateDaysLabel.setText("");
    }

    private void loadEligibleRentals() {
        rentalComboBox.removeAllItems();
        List<Rental> rentals = rentalService.getUserRentals(currentUser);
        for (Rental r : rentals) {
            if (r.getStatus() == RentalStatus.ACTIVE || 
                r.getStatus() == RentalStatus.REJECTED) {
                String item = r.getRentalId() + " - " + r.getEquipment().getName();
                rentalComboBox.addItem(item);
            }
        }
        if (rentalComboBox.getItemCount() == 0) {
            rentalComboBox.addItem("No eligible rentals");
        }
    }

    private void updateLateDaysInfo() {
        String selected = (String) rentalComboBox.getSelectedItem();
        if (selected == null || selected.equals("No eligible rentals")) {
            lateDaysLabel.setText("");
            return;
        }

        String rentalId = selected.split(" - ")[0];
        List<Rental> rentals = rentalService.getUserRentals(currentUser);
        for (Rental r : rentals) {
            if (r.getRentalId().equals(rentalId)) {
                int lateDays = r.getLateDays();
                if (lateDays > 0) {
                    lateDaysLabel.setText("Late return! Current late days: " + lateDays);
                } else {
                    lateDaysLabel.setText("On time or early return");
                }
                break;
            }
        }
    }

    private void submitReturn() {
        String selected = (String) rentalComboBox.getSelectedItem();
        if (selected == null || selected.equals("No eligible rentals")) {
            resultArea.setText("Error: No eligible rentals found");
            return;
        }

        String rentalId = selected.split(" - ")[0];
        DamageSeverity severity = (DamageSeverity) severityComboBox.getSelectedItem();

        String result = rentalService.submitReturnRequest(rentalId, severity.name());
        
        if (result.equals("SUCCESS")) {
            resultArea.setText("Return Request Submitted Successfully!\n\n" +
                             "Rental ID: " + rentalId + "\n" +
                             "Reported Damage: " + severity.name() + "\n\n" +
                             "Your request is now pending Staff approval.\n" +
                             "You can check the status in 'My Rentals' tab.");
            loadEligibleRentals();
        } else {
            resultArea.setText("Error: " + result);
        }
    }
}
