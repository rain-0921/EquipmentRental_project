package gui;

import model.Bill;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BillingPanel extends JPanel {

    private final List<Bill> bills = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextArea detailArea;

    public BillingPanel() {
        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(28, 32, 28, 32));
        setBackground(UIStyle.CONTENT_BG);

        add(buildHeader(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Rental ID", "Equipment", "Renter", "Pricing Plan", "Net Payable (RM)"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIStyle.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelectedDetail();
        });

        detailArea = new JTextArea();
        detailArea.setFont(UIStyle.FONT_MONO);
        detailArea.setEditable(false);
        detailArea.setBackground(UIStyle.CARD_BG);
        detailArea.setForeground(UIStyle.TEXT_DARK);
        detailArea.setBorder(new EmptyBorder(16, 16, 16, 16));
        detailArea.setText("Select a rental from the list above to view the detailed bill.");

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                UIStyle.card(new JScrollPane(table)),
                UIStyle.card(new JScrollPane(detailArea)));
        split.setResizeWeight(0.5);
        split.setBorder(null);
        split.setDividerSize(10);
        split.setOpaque(false);

        add(split, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Billing History");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);
        JLabel subtitle = new JLabel("Detailed rental bills: base fee, discount, penalty, and net payable");
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

    public void addBill(Bill bill) {
        bills.add(bill);
        tableModel.addRow(new Object[]{
                bill.getRental().getRentalId(),
                bill.getRental().getEquipment().getName(),
                bill.getRental().getUser().getFullName(),
                bill.getStrategyUsed(),
                String.format("%.2f", bill.getNetPayable())
        });
    }

    private void showSelectedDetail() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= bills.size()) return;
        detailArea.setText(bills.get(row).toDetailedString());
        detailArea.setCaretPosition(0);
    }
}
