package gui;

import model.equipment.EquipmentItem;
import model.equipment.EquipmentStatus;
import repository.EquipmentRepository;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

/**
 * Browse-and-filter the equipment catalogue. Also doubles as the
 * equipment picker for the "Rent" panel.
 */
public class EquipmentPanel extends JPanel {

    private final EquipmentRepository repo = new EquipmentRepository();
    private final JTable table;
    private final DefaultTableModel model;

    public EquipmentPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        setBorder(Theme.padded(Theme.PAD));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = Theme.pageTitle("Browse Equipment");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub   = Theme.subtitle("All equipment in the catalogue and their current availability.");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        String[] cols = {
            "Equipment ID", "Name", "Category",
            "Daily Rate (RM)", "Pricing Policy", "Status"
        };
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(28);
        table.setFont(Theme.bodyFont());
        table.getTableHeader().setFont(Theme.buttonFont());
        table.getTableHeader().setBackground(Theme.SURFACE);
        table.getTableHeader().setForeground(Theme.TEXT);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setFillsViewportHeight(true);

        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(JLabel.CENTER);
                l.setFont(Theme.buttonFont().deriveFont(10f));
                l.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                if (!isSelected) {
                    if ("Available".equals(value)) {
                        l.setBackground(Theme.SUCCESS);
                    } else {
                        l.setBackground(Theme.WARN);
                    }
                    l.setForeground(Color.WHITE);
                    l.setOpaque(true);
                }
                return l;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        refresh();
    }

    public final void refresh() {
        model.setRowCount(0);
        try {
            List<EquipmentItem> all = repo.findAll();
            for (EquipmentItem e : all) {
                model.addRow(new Object[] {
                    e.getEquipmentId(),
                    e.getName(),
                    e.getCategoryDisplayName(),
                    String.format("%.2f", e.getDailyRate()),
                    e.getPricingPolicy().getPolicyName(),
                    e.isAvailable() ? EquipmentStatus.AVAILABLE.getDisplayName()
                                    : EquipmentStatus.RENTED.getDisplayName()
                });
            }
        } catch (Exception ex) {
            GuiUtil.showError(this, "Failed to load equipment",
                "Could not load equipment list:\n" + ex.getMessage());
        }
    }

    /** @return the selected {@link EquipmentItem}, or empty. */
    public java.util.Optional<EquipmentItem> getSelectedEquipment() {
        int row = table.getSelectedRow();
        if (row < 0) return java.util.Optional.empty();
        String id = (String) model.getValueAt(table.convertRowIndexToModel(row), 0);
        try {
            return repo.findAll().stream()
                .filter(e -> e.getEquipmentId().equals(id))
                .findFirst();
        } catch (Exception ex) {
            GuiUtil.showError(this, "Lookup failed", ex.getMessage());
            return java.util.Optional.empty();
        }
    }
}