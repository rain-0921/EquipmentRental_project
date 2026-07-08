package rental.gui;

import rental.model.user.User;
import rental.service.ApprovalService;
import rental.model.rental.Rental;
import rental.model.penalty.DamageSeverity;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ApprovalPanel extends JPanel {
    private User currentUser;
    private ApprovalService approvalService;
    private JTable approvalsTable;
    private DefaultTableModel tableModel;

    public ApprovalPanel(User user) {
        this.currentUser = user;
        this.approvalService = ApprovalService.getInstance();
        initComponents();
        loadPendingApprovals();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 242, 245));

        JLabel titleLabel = new JLabel("Pending Return Approvals");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        topPanel.add(titleLabel, BorderLayout.WEST);

        String[] columns = {"Rental ID", "Equipment", "Renter", "Days", "Due Date", "Reported Severity"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        approvalsTable = new JTable(tableModel);
        approvalsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        approvalsTable.setRowHeight(28);
        approvalsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        approvalsTable.getTableHeader().setBackground(new Color(233, 236, 239));
        approvalsTable.setGridColor(new Color(222, 226, 230));
        JScrollPane scrollPane = new JScrollPane(approvalsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        JButton approveButton = new JButton("Approve Return");
        approveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        approveButton.setBackground(new Color(40, 167, 69));
        approveButton.setForeground(Color.WHITE);
        approveButton.setFocusPainted(false);
        approveButton.setBorderPainted(false);
        approveButton.setOpaque(true);
        approveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        approveButton.addActionListener(e -> approveReturn());

        JButton rejectButton = new JButton("Reject Return");
        rejectButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rejectButton.setBackground(new Color(220, 53, 69));
        rejectButton.setForeground(Color.WHITE);
        rejectButton.setFocusPainted(false);
        rejectButton.setBorderPainted(false);
        rejectButton.setOpaque(true);
        rejectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rejectButton.addActionListener(e -> rejectReturn());

        buttonPanel.add(approveButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(rejectButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void refreshData() {
        loadPendingApprovals();
    }

    private void loadPendingApprovals() {
        tableModel.setRowCount(0);
        List<Rental> pending = approvalService.getPendingApprovals();
        for (Rental r : pending) {
            Object[] row = {
                r.getRentalId(),
                r.getEquipment().getName(),
                r.getUser().getName(),
                r.getRentalDays(),
                r.getDueDate().toString(),
                r.getReportedSeverity() != null ? r.getReportedSeverity().name() : "N/A"
            };
            tableModel.addRow(row);
        }
    }

    private void approveReturn() {
        int selectedRow = approvalsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a rental to approve",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rentalId = (String) tableModel.getValueAt(selectedRow, 0);
        
        List<Rental> pending = approvalService.getPendingApprovals();
        Rental selectedRental = null;
        for (Rental r : pending) {
            if (r.getRentalId().equals(rentalId)) {
                selectedRental = r;
                break;
            }
        }

        if (selectedRental == null) return;

        DamageSeverity currentSeverity = selectedRental.getReportedSeverity();
        DamageSeverity[] severities = DamageSeverity.values();
        DamageSeverity selectedSeverity = (DamageSeverity) JOptionPane.showInputDialog(
            this,
            "Confirm Damage Severity:\n(Current reported: " + currentSeverity + ")",
            "Approve Return",
            JOptionPane.QUESTION_MESSAGE,
            null,
            severities,
            currentSeverity
        );

        if (selectedSeverity == null) return;

        String result = approvalService.approveReturn(rentalId, selectedSeverity);
        
        if (result.equals("SUCCESS")) {
            JOptionPane.showMessageDialog(this, 
                "Return Approved Successfully!\n\n" +
                "Rental ID: " + rentalId + "\n" +
                "Final Severity: " + selectedSeverity.name() + "\n" +
                "Bill has been generated.",
                "Approval Complete", JOptionPane.INFORMATION_MESSAGE);
            loadPendingApprovals();
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + result,
                "Approval Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectReturn() {
        int selectedRow = approvalsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a rental to reject",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rentalId = (String) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reject this return?\n" +
            "Rental ID: " + rentalId + "\n\n" +
            "The renter will need to submit a new return request.",
            "Confirm Rejection",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String result = approvalService.rejectReturn(rentalId);
        
        if (result.equals("SUCCESS")) {
            JOptionPane.showMessageDialog(this, 
                "Return Rejected Successfully!\n\n" +
                "Rental ID: " + rentalId + "\n" +
                "The renter can now submit a new return request.",
                "Rejection Complete", JOptionPane.INFORMATION_MESSAGE);
            loadPendingApprovals();
        } else {
            JOptionPane.showMessageDialog(this, "Error: " + result,
                "Rejection Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
