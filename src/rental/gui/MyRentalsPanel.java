package rental.gui;

import rental.model.user.User;
import rental.service.RentalService;
import rental.model.rental.Rental;
import rental.model.rental.RentalStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyRentalsPanel extends JPanel {
    private User currentUser;
    private RentalService rentalService;
    private JTable rentalsTable;
    private DefaultTableModel tableModel;

    public MyRentalsPanel(User user) {
        this.currentUser = user;
        this.rentalService = RentalService.getInstance();
        initComponents();
        loadRentals();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("My Rentals");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topPanel.add(titleLabel, BorderLayout.WEST);

        String[] columns = {"Rental ID", "Equipment", "Days", "Rental Date", "Due Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rentalsTable = new JTable(tableModel);
        rentalsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rentalsTable.setRowHeight(28);
        rentalsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        rentalsTable.getTableHeader().setBackground(new Color(233, 236, 239));
        rentalsTable.setGridColor(new Color(222, 226, 230));
        JScrollPane scrollPane = new JScrollPane(rentalsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        loadRentals();
    }

    public void loadRentals() {
        tableModel.setRowCount(0);
        List<Rental> rentals = rentalService.getUserRentals(currentUser);
        for (Rental r : rentals) {
            Object[] row = {
                r.getRentalId(),
                r.getEquipment().getName(),
                r.getRentalDays(),
                r.getRentalDate().toString(),
                r.getDueDate().toString(),
                r.getStatus().name()
            };
            tableModel.addRow(row);
        }
    }
}
