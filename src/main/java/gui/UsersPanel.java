package gui;

import model.user.Student;
import model.user.User;
import model.user.UserType;
import repository.UserRepository;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

/** Staff-only table of every registered user. */
public class UsersPanel extends JPanel {

    private final UserRepository repo = new UserRepository();
    private final DefaultTableModel model;
    private final JTable table;

    public UsersPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        setBorder(Theme.padded(Theme.PAD));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = Theme.pageTitle("Manage Users");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub   = Theme.subtitle("All registered students and staff. Use the toolbar to add new users or toggle final-year status.");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 14, 0));
        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        JButton createBtn = Theme.primaryButton("+ Create User");
        JButton toggleBtn = Theme.ghostButton("Toggle Final-Year");
        JPanel toolbar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        toolbar.add(createBtn);
        toolbar.add(toggleBtn);
        card.add(toolbar, BorderLayout.NORTH);

        String[] cols = { "User ID", "Full Name", "Email", "Type",
                          "Discount", "Notes" };
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

        // Colour the "Notes" cell so the final-year badge is easy to spot.
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel c = (JLabel) super.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, column);
                c.setHorizontalAlignment(JLabel.CENTER);
                c.setFont(Theme.buttonFont().deriveFont(10f));
                c.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
                if (!isSelected) {
                    if ("Final Year".equals(value)) {
                        c.setBackground(Theme.SUCCESS);
                        c.setForeground(Color.WHITE);
                        c.setOpaque(true);
                        c.setText("FINAL YEAR");
                    } else {
                        c.setOpaque(false);
                    }
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1, true));
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        createBtn.addActionListener(e -> onCreateClicked());
        toggleBtn.addActionListener(e -> onToggleFinalYear());

        refresh();
    }

    private void onCreateClicked() {
        CreateUserDialog dlg = new CreateUserDialog(
            (javax.swing.JFrame) SwingUtilities.getWindowAncestor(this));
        dlg.setVisible(true);
        if (dlg.getCreatedUser() != null) {
            refresh();
        }
    }

    private void onToggleFinalYear() {
        int row = table.getSelectedRow();
        if (row < 0) {
            GuiUtil.showInfo(this, "No selection",
                "Pick a student first to toggle their final-year status.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String userId = (String) model.getValueAt(modelRow, 0);
        String type   = (String) model.getValueAt(modelRow, 3);
        if (!"Student".equals(type)) {
            JOptionPane.showMessageDialog(this,
                "Only students can be marked as final year.",
                "Not a student", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            User u = repo.findById(userId).orElseThrow(
                () -> new IllegalStateException("User not found: " + userId));
            if (!(u instanceof Student s)) {
                JOptionPane.showMessageDialog(this,
                    "Selected user is not a student record.",
                    "Wrong type", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean newVal = !s.isFinalYear();
            try (var c = db.DatabaseManager.getInstance().getConnection();
                 var ps = c.prepareStatement(
                     "UPDATE user SET is_final_year = ? WHERE user_id = ?")) {
                ps.setBoolean(1, newVal);
                ps.setString(2, userId);
                ps.executeUpdate();
            }
            refresh();
        } catch (Exception ex) {
            GuiUtil.showError(this, "Toggle failed", ex.getMessage());
        }
    }

    public final void refresh() {
        model.setRowCount(0);
        try {
            List<User> users = repo.findAll();
            for (User u : users) {
                model.addRow(new Object[] {
                    u.getUserId(), u.getFullName(), u.getEmail(),
                    u.getType().getDisplayName(),
                    String.format("%.1f%%", u.getDiscountRate() * 100),
                    u.isFinalYear() ? "Final Year" : ""
                });
            }
        } catch (Exception ex) {
            GuiUtil.showError(this, "Failed to load users",
                "Could not load users:\n" + ex.getMessage());
        }
    }
}