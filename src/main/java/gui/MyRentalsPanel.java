package gui;

import model.equipment.EquipmentItem;
import model.equipment.EquipmentStatus;
import model.rental.Rental;
import repository.EquipmentRepository;
import repository.RentalRepository;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rentals table. If {@code userIdFilter} is non-null, only that
 * user's rentals are shown (used for the student view). If null,
 * every rental in the system is shown (staff view).
 *
 * The Status column shows the effective equipment status:
 *   - "Available"   (green) - rental returned, equipment free to rent
 *   - "Rented"      (amber) - rental still in flight
 *   - "Overdue"     (red)   - past due date
 *
 * The Damage column shows the severity tier (None / Light /
 * Moderate / Heavy) with a colored badge so it's easy to spot.
 */
public class MyRentalsPanel extends JPanel {

    private final String userIdFilter;
    private final RentalRepository repo = new RentalRepository();
    private final EquipmentRepository equipRepo = new EquipmentRepository();
    private final DefaultTableModel model;
    private final JTable table;

    public MyRentalsPanel(String userIdFilter) {
        this.userIdFilter = userIdFilter;
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        setBorder(Theme.padded(Theme.PAD));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = Theme.pageTitle(userIdFilter == null ? "All Rentals" : "My Rentals");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub   = Theme.subtitle(userIdFilter == null
            ? "Every rental in the system. Returned items show as Available."
            : "Your rental history. Returned items show as Available.");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        String[] cols = { "Rental ID", "Equipment ID", "User", "Rent Date", "Due Date",
                          "Return Date", "Status", "Damage" };
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

        // Status column - colored badge per row.
        table.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer(
            new Color[] { Theme.MUTED, Theme.WARN, Theme.SUCCESS, Theme.DANGER, Theme.MUTED },
            new String[] { "AVAILABLE", "RENTED", "AVAILABLE", "OVERDUE", "RETURNED" }));
        // Damage column - colored badge per severity.
        table.getColumnModel().getColumn(7).setCellRenderer(new BadgeRenderer(
            new Color[] { Theme.MUTED, Theme.WARN, Theme.WARN, Theme.DANGER },
            new String[] { "NONE", "LIGHT", "MODERATE", "HEAVY" }));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        refresh();
    }

    public final void refresh() {
        model.setRowCount(0);
        try {
            // Cache the equipment-id -> availability map so the
            // Status column can show "Available" for returned items.
            Map<String, Boolean> equipAvail = new HashMap<>();
            for (EquipmentItem e : equipRepo.findAll()) {
                equipAvail.put(e.getEquipmentId(), e.isAvailable());
            }

            List<Rental> rentals = userIdFilter == null
                ? repo.findAll()
                : repo.findByUser(userIdFilter);
            for (Rental r : rentals) {
                String displayStatus;
                Color  statusColor;
                if (r.getStatus() == Rental.Status.RETURNED) {
                    displayStatus = "AVAILABLE";
                    statusColor   = Theme.SUCCESS;
                } else if (r.getStatus() == Rental.Status.OVERDUE) {
                    displayStatus = "OVERDUE";
                    statusColor   = Theme.DANGER;
                } else if (Boolean.TRUE.equals(equipAvail.get(r.getEquipmentId()))) {
                    displayStatus = "AVAILABLE";
                    statusColor   = Theme.SUCCESS;
                } else {
                    displayStatus = "RENTED";
                    statusColor   = Theme.WARN;
                }
                model.addRow(new Object[] {
                    r.getRentalId(),
                    r.getEquipmentId(),
                    r.getUserId(),
                    r.getRentDate(),
                    r.getDueDate(),
                    r.getReturnDate(),
                    new Badge(statusColor, displayStatus),
                    new Badge(severityColor(r.getDamageLevel()),
                              r.getDamageLevel().name())
                });
            }
        } catch (Exception ex) {
            GuiUtil.showError(this, "Failed to load rentals",
                "Could not load rentals:\n" + ex.getMessage());
        }
    }

    private static Color severityColor(Rental.DamageLevel d) {
        switch (d) {
            case LIGHT:    return Theme.WARN;
            case MODERATE: return Theme.WARN;
            case HEAVY:    return Theme.DANGER;
            default:       return Theme.MUTED;
        }
    }

    /** Simple value object that the cell renderer uses to draw a
     *  pill-shaped coloured badge. */
    private static final class Badge {
        final Color color;
        final String text;
        Badge(Color c, String t) { this.color = c; this.text = t; }
    }

    /** Renders a Badge as a centered pill, with fallbacks for plain
     *  Strings or any other cell value. */
    private static final class BadgeRenderer extends DefaultTableCellRenderer {
        private final Color[] palette;
        private final String[] labels;
        BadgeRenderer(Color[] palette, String[] labels) {
            this.palette = palette;
            this.labels  = labels;
        }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(
                t, value, isSelected, hasFocus, row, column);
            l.setHorizontalAlignment(JLabel.CENTER);
            l.setFont(Theme.buttonFont().deriveFont(10f));
            l.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

            Color fill;
            String text;
            if (value instanceof Badge b) {
                fill = b.color;
                text = b.text;
            } else {
                text = value == null ? "" : value.toString();
                fill = palette[Math.min(Math.max(0, indexOf(text)), palette.length - 1)];
            }
            if (isSelected) {
                l.setBackground(t.getSelectionBackground());
                l.setForeground(t.getSelectionForeground());
            } else {
                l.setBackground(fill);
                l.setForeground(Color.WHITE);
            }
            l.setOpaque(true);
            l.setText(text);
            return l;
        }

        private int indexOf(String s) {
            for (int i = 0; i < labels.length; i++) {
                if (labels[i].equalsIgnoreCase(s)) return i;
            }
            return 0;
        }
    }
}