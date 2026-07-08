package rental.gui;

import rental.model.user.User;
import rental.model.user.UserRole;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private User currentUser;
    private JTabbedPane tabbedPane;

    public MainFrame(User user) {
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        String title = "Campus Equipment Rental - " + currentUser.getName() + 
                      " (" + currentUser.getRole() + ")";
        setTitle(title);
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 123, 255));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(e -> performLogout());

        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        EquipmentPanel equipmentPanel = new EquipmentPanel(currentUser);
        RentalPanel rentalPanel = new RentalPanel(currentUser);
        MyRentalsPanel myRentalsPanel = new MyRentalsPanel(currentUser);
        SubmitReturnPanel submitReturnPanel = new SubmitReturnPanel(currentUser);
        MyBillsPanel myBillsPanel = new MyBillsPanel(currentUser);

        tabbedPane.addTab("Equipment Catalog", equipmentPanel);
        tabbedPane.addTab("New Rental", rentalPanel);
        tabbedPane.addTab("My Rentals", myRentalsPanel);
        tabbedPane.addTab("Submit Return", submitReturnPanel);
        tabbedPane.addTab("My Bills", myBillsPanel);

        if (currentUser.getRole() == UserRole.STAFF) {
            tabbedPane.addTab("Pending Approvals", new ApprovalPanel(currentUser));
            tabbedPane.addTab("Equipment Admin", new EquipmentAdminPanel(currentUser));
            tabbedPane.addTab("User Admin", new UserAdminPanel(currentUser));
            tabbedPane.addTab("Billing History", new BillingHistoryPanel(currentUser));
        }

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            Component selectedComponent = tabbedPane.getComponentAt(selectedIndex);
            
            if (selectedComponent instanceof EquipmentPanel) {
                ((EquipmentPanel) selectedComponent).refreshData();
            } else if (selectedComponent instanceof RentalPanel) {
                ((RentalPanel) selectedComponent).refreshData();
            } else if (selectedComponent instanceof MyRentalsPanel) {
                ((MyRentalsPanel) selectedComponent).refreshData();
            } else if (selectedComponent instanceof SubmitReturnPanel) {
                ((SubmitReturnPanel) selectedComponent).refreshData();
            } else if (selectedComponent instanceof MyBillsPanel) {
                ((MyBillsPanel) selectedComponent).refreshData();
            } else if (selectedComponent instanceof ApprovalPanel) {
                ((ApprovalPanel) selectedComponent).refreshData();
            } else if (selectedComponent instanceof EquipmentAdminPanel) {
                ((EquipmentAdminPanel) selectedComponent).refreshData();
            } else if (selectedComponent instanceof UserAdminPanel) {
                ((UserAdminPanel) selectedComponent).refreshData();
            } else if (selectedComponent instanceof BillingHistoryPanel) {
                ((BillingHistoryPanel) selectedComponent).refreshData();
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                int choice = JOptionPane.showConfirmDialog(MainFrame.this,
                    "Are you sure you want to exit?", "Confirm Exit",
                    JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        });
    }

    private void performLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
