package rental.gui;

import rental.model.user.User;
import rental.repo.BillRepository;
import rental.model.billing.Bill;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MyBillsPanel extends JPanel {
    private User currentUser;
    private BillRepository billRepository;
    private JTable billsTable;
    private DefaultTableModel tableModel;

    public MyBillsPanel(User user) {
        this.currentUser = user;
        this.billRepository = BillRepository.getInstance();
        initComponents();
        loadBills();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("My Bills");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topPanel.add(titleLabel, BorderLayout.WEST);

        String[] columns = {"Bill ID", "Equipment", "Pricing Plan", "Subtotal", "Discount", 
                           "Late Penalty", "Damage Penalty", "Net Payable"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        billsTable = new JTable(tableModel);
        billsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        billsTable.setRowHeight(28);
        billsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        billsTable.getTableHeader().setBackground(new Color(233, 236, 239));
        billsTable.setGridColor(new Color(222, 226, 230));
        JScrollPane scrollPane = new JScrollPane(billsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void refreshData() {
        loadBills();
    }

    public void loadBills() {
        tableModel.setRowCount(0);
        List<Bill> bills = billRepository.getBillsByUser(currentUser);
        for (Bill bill : bills) {
            Object[] row = {
                bill.getBillId(),
                bill.getEquipmentName(),
                bill.getPricingPlan(),
                String.format("RM %.2f", bill.getSubtotal()),
                String.format("RM %.2f", bill.getDiscount()),
                String.format("RM %.2f", bill.getLatePenalty()),
                String.format("RM %.2f", bill.getDamagePenalty()),
                String.format("RM %.2f", bill.getNetPayable())
            };
            tableModel.addRow(row);
        }
    }
}
