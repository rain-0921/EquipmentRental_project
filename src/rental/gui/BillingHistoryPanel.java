package rental.gui;

import rental.model.user.User;
import rental.repo.BillRepository;
import rental.model.billing.Bill;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BillingHistoryPanel extends JPanel {
    private User currentUser;
    private BillRepository billRepository;
    private JTable billsTable;
    private DefaultTableModel tableModel;

    public BillingHistoryPanel(User user) {
        this.currentUser = user;
        this.billRepository = BillRepository.getInstance();
        initComponents();
        loadBills();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("Billing History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topPanel.add(titleLabel, BorderLayout.WEST);

        String[] columns = {"Rental ID", "Equipment", "Renter", "Pricing Plan", 
                           "Subtotal", "Discount", "Late", "Damage", "Net Payable"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        billsTable = new JTable(tableModel);
        billsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        billsTable.setRowHeight(28);
        billsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        billsTable.getTableHeader().setBackground(new Color(233, 236, 239));
        billsTable.setGridColor(new Color(222, 226, 230));
        JScrollPane scrollPane = new JScrollPane(billsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JButton viewDetailButton = new JButton("View Bill Details");
        viewDetailButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewDetailButton.setBackground(new Color(0, 123, 255));
        viewDetailButton.setForeground(Color.WHITE);
        viewDetailButton.setFocusPainted(false);
        viewDetailButton.setBorderPainted(false);
        viewDetailButton.setOpaque(true);
        viewDetailButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewDetailButton.addActionListener(e -> viewBillDetails());

        buttonPanel.add(viewDetailButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        loadBills();
    }

    private void loadBills() {
        tableModel.setRowCount(0);
        List<Bill> bills = billRepository.getAllBills();
        for (Bill bill : bills) {
            Object[] row = {
                bill.getRentalId(),
                bill.getEquipmentName(),
                bill.getRenterName(),
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

    private void viewBillDetails() {
        int selectedRow = billsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a bill to view details");
            return;
        }

        String rentalId = (String) tableModel.getValueAt(selectedRow, 0);
        
        List<Bill> bills = billRepository.getAllBills();
        Bill selectedBill = null;
        for (Bill b : bills) {
            if (b.getRentalId().equals(rentalId)) {
                selectedBill = b;
                break;
            }
        }

        if (selectedBill == null) return;

        String details = String.format(
            "===== BILL DETAILS =====\n\n" +
            "Bill ID:        %s\n" +
            "Rental ID:      %s\n" +
            "Equipment:      %s\n" +
            "Renter:         %s\n" +
            "Pricing Plan:   %s\n\n" +
            "----- CHARGES -----\n" +
            "Subtotal:       RM %.2f\n" +
            "Discount:       - RM %.2f\n" +
            "Late Penalty:   RM %.2f\n" +
            "Damage Penalty: RM %.2f\n" +
            "------------------------\n" +
            "NET PAYABLE:    RM %.2f\n",
            selectedBill.getBillId(),
            selectedBill.getRentalId(),
            selectedBill.getEquipmentName(),
            selectedBill.getRenterName(),
            selectedBill.getPricingPlan(),
            selectedBill.getSubtotal(),
            selectedBill.getDiscount(),
            selectedBill.getLatePenalty(),
            selectedBill.getDamagePenalty(),
            selectedBill.getNetPayable()
        );

        JTextArea textArea = new JTextArea(details);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setBackground(new Color(250, 250, 250));

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea),
            "Bill Details", JOptionPane.INFORMATION_MESSAGE);
    }
}
