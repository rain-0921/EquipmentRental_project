package gui;

import model.equipment.EquipmentItem;
import model.equipment.EquipmentStatus;
import model.user.User;
import model.user.UserType;
import service.RentalService;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.Vector;

/**
 * Self-service "rent equipment" form. Available to both students
 * (self-service) and staff (since v3). Pick an available item, a
 * rent date, a due date, then click Confirm Rent. The service layer
 * takes care of the rest.
 *
 * After a successful rental the panel refreshes its own picker and
 * pokes the supplied {@link #onRentalCreated} callback so the shell
 * can immediately refresh the My Rentals / All Rentals view - no
 * need to log out and back in to see the new row.
 */
public class RentPanel extends JPanel {

    private final User currentUser;
    private final JComboBox<EquipmentItem> equipmentBox;
    private final JLabel equipmentSummary = new JLabel(" ", SwingConstants.LEFT);
    private final DatePickerField rentPicker;
    private final DatePickerField duePicker;
    private final JButton rentButton = Theme.successButton("Confirm Rent");
    private final JLabel  statusLabel = new JLabel(" ", SwingConstants.LEFT);

    private final RentalService rentalService = new RentalService();

    /** Called immediately after a successful rental so the shell can
     *  refresh sibling views (e.g. My Rentals) without a re-login. */
    private Runnable onRentalCreated;

    public RentPanel(User currentUser) {
        this(currentUser, null);
    }

    public RentPanel(User currentUser, Runnable onRentalCreated) {
        this.currentUser = currentUser;
        this.onRentalCreated = onRentalCreated;
        setLayout(new BorderLayout());
        setBackground(Theme.SURFACE);
        setBorder(Theme.padded(Theme.PAD));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD);
        card.setBorder(Theme.cardBorder());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = Theme.pageTitle("Rent equipment");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub   = Theme.subtitle(
            currentUser.getType() == UserType.STAFF
                ? "As staff, you can rent equipment on behalf of a student or for your own use."
                : "Pick an available item, choose rent and due dates, then click Confirm Rent.");
        sub.setAlignmentX(LEFT_ALIGNMENT);
        sub.setBorder(BorderFactory.createEmptyBorder(4, 0, 14, 0));
        header.add(title);
        header.add(sub);
        card.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(Theme.padded(6));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        equipmentBox = new JComboBox<>(loadAvailableItems());
        equipmentBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof EquipmentItem e) {
                    setText(String.format("%s  -  %s  (RM %.2f/day)",
                        e.getEquipmentId(), e.getName(), e.getDailyRate()));
                }
                return this;
            }
        });
        equipmentBox.setFont(Theme.bodyFont());
        equipmentBox.setBackground(Theme.CARD);
        equipmentBox.setBorder(Theme.fieldBorder());
        equipmentBox.setPreferredSize(new java.awt.Dimension(360, 34));
        equipmentSummary.setBorder(BorderFactory.createEmptyBorder());
        equipmentSummary.setOpaque(false);
        equipmentSummary.setForeground(Theme.MUTED);
        equipmentSummary.setFont(Theme.smallFont());
        updateEquipmentSummary();
        equipmentBox.addActionListener(e -> updateEquipmentSummary());

        LocalDate today = LocalDate.now();
        rentPicker = new DatePickerField(today);
        rentPicker.setMinDate(today);
        duePicker  = new DatePickerField(today.plusDays(7));
        duePicker.setMinDate(today);

        // Whenever rent date changes, push the due date forward if it
        // would now be before the rent date - saves the user from a
        // "Due date is before rent date" validation error.
        rentPicker.addChangeListener(r -> {
            if (r == null) return;
            LocalDate due = duePicker.getValue();
            if (due == null || due.isBefore(r)) {
                duePicker.setValue(r.plusDays(7));
            } else {
                duePicker.setMinDate(r);
            }
        });

        JButton plus7 = Theme.ghostButton("+7 days");
        plus7.setPreferredSize(new java.awt.Dimension(110, 32));
        plus7.addActionListener(e -> {
            LocalDate due = duePicker.getValue();
            LocalDate base = due != null ? due : LocalDate.now();
            duePicker.setValue(base.plusDays(7));
        });

        rentButton.setPreferredSize(new java.awt.Dimension(180, 38));

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(formLabel("Available equipment"), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(equipmentBox, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(new JLabel(""), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(equipmentSummary, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(formLabel("Rent date"), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        JPanel rentRow = new JPanel(new BorderLayout());
        rentRow.setOpaque(false);
        rentRow.add(rentPicker, BorderLayout.CENTER);
        form.add(rentRow, c);

        c.gridx = 0; c.gridy = 3; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(formLabel("Due date"), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        JPanel dueRow = new JPanel(new BorderLayout(8, 0));
        dueRow.setOpaque(false);
        dueRow.add(duePicker, BorderLayout.CENTER);
        dueRow.add(plus7, BorderLayout.EAST);
        form.add(dueRow, c);

        c.gridx = 0; c.gridy = 4; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(new JLabel(""), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(rentButton, c);

        // Hint row below the form
        JPanel hints = new JPanel(new GridLayout(2, 1));
        hints.setOpaque(false);
        hints.setBorder(new EmptyBorder(6, 12, 0, 12));
        hints.add(rentPicker.getHintComponent());
        hints.add(duePicker.getHintComponent());

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.add(form, BorderLayout.NORTH);
        body.add(hints, BorderLayout.CENTER);

        card.add(body, BorderLayout.NORTH);

        add(card, BorderLayout.NORTH);

        statusLabel.setFont(Theme.bodyFont());
        statusLabel.setBorder(new EmptyBorder(12, 4, 0, 4));
        add(statusLabel, BorderLayout.CENTER);

        rentButton.addActionListener(e -> onRentClicked());
    }

    private static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bodyFont());
        l.setForeground(Theme.MUTED);
        return l;
    }

    private Vector<EquipmentItem> loadAvailableItems() {
        Vector<EquipmentItem> items = new Vector<>();
        try {
            for (EquipmentItem e : new repository.EquipmentRepository().findAll()) {
                if (e.isAvailable()) items.add(e);
            }
        } catch (Exception ex) {
            GuiUtil.showError(this, "Failed to load equipment",
                "Could not load equipment list:\n" + ex.getMessage());
        }
        return items;
    }

    private void updateEquipmentSummary() {
        EquipmentItem e = (EquipmentItem) equipmentBox.getSelectedItem();
        if (e == null) {
            equipmentSummary.setText("");
        } else {
            equipmentSummary.setText(String.format(
                "%s - %s - RM %.2f/day - %s",
                e.getCategoryDisplayName(),
                e.getPricingPolicy().getPolicyName(),
                e.getDailyRate(),
                EquipmentStatus.AVAILABLE.getDisplayName()));
        }
    }

    private void onRentClicked() {
        EquipmentItem e = (EquipmentItem) equipmentBox.getSelectedItem();
        if (e == null) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("Please choose an item to rent.");
            return;
        }
        LocalDate rentDate = rentPicker.getValue();
        LocalDate dueDate  = duePicker.getValue();
        LocalDate today    = LocalDate.now();

        if (rentDate == null || dueDate == null) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("Please choose both rent and due dates.");
            return;
        }
        if (rentDate.isBefore(today)) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("Rent date cannot be in the past.");
            return;
        }
        if (dueDate.isBefore(today)) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("Due date cannot be in the past.");
            return;
        }
        if (dueDate.isBefore(rentDate)) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("Due date cannot be before the rent date.");
            return;
        }

        try {
            rentalService.rentEquipment(currentUser.getUserId(),
                e.getEquipmentId(), rentDate, dueDate);
            statusLabel.setForeground(Theme.SUCCESS);
            statusLabel.setText(String.format(
                "Rental created for %s. Due back on %s.",
                e.getName(), dueDate));

            equipmentBox.setModel(new DefaultComboBoxModel<>(loadAvailableItems()));
            updateEquipmentSummary();

            if (onRentalCreated != null) {
                try { onRentalCreated.run(); }
                catch (Exception ignored) { /* best-effort refresh */ }
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            GuiUtil.showError(this, "Rental failed", ex.getMessage());
        }
    }
}