package rental.gui;

import rental.model.user.User;
import rental.service.RentalService;
import rental.model.equipment.Equipment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EquipmentPanel extends JPanel {
    private User currentUser;
    private RentalService rentalService;
    private JTable equipmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public EquipmentPanel(User user) {
        this.currentUser = user;
        this.rentalService = RentalService.getInstance();
        initComponents();
        loadEquipment();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("Equipment Catalog");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        searchField = new JTextField(28);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JButton searchButton = new JButton("Search");
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.setBackground(new Color(0, 123, 255));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setOpaque(true);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        refreshButton.setBackground(new Color(108, 117, 125));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setOpaque(true);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(8));
        searchPanel.add(searchButton);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(refreshButton);

        String[] columns = {"ID", "Name", "Category", "Status", "Daily Rate (RM)"};
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

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        searchButton.addActionListener(e -> searchEquipment());
        refreshButton.addActionListener(e -> refreshData());
        searchField.addActionListener(e -> searchEquipment());
    }

    public void refreshData() {
        searchField.setText("");
        loadEquipment();
    }

    private void loadEquipment() {
        tableModel.setRowCount(0);
        List<Equipment> equipmentList = rentalService.getAllEquipment();
        for (Equipment eq : equipmentList) {
            Object[] row = {
                eq.getEquipmentId(),
                eq.getName(),
                eq.getCategory().name(),
                eq.getStatus().name(),
                String.format("%.2f", eq.getDailyRate())
            };
            tableModel.addRow(row);
        }
    }

    private void searchEquipment() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        List<Equipment> equipmentList;
        if (keyword.isEmpty()) {
            equipmentList = rentalService.getAllEquipment();
        } else {
            equipmentList = rentalService.searchEquipment(keyword);
        }
        for (Equipment eq : equipmentList) {
            Object[] row = {
                eq.getEquipmentId(),
                eq.getName(),
                eq.getCategory().name(),
                eq.getStatus().name(),
                String.format("%.2f", eq.getDailyRate())
            };
            tableModel.addRow(row);
        }
    }
}
