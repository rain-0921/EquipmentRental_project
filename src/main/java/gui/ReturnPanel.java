package gui;

import model.billing.Bill;
import model.equipment.EquipmentItem;
import model.rental.Rental;
import model.rental.Rental.DamageLevel;
import repository.EquipmentRepository;
import repository.RentalRepository;
import repository.UserRepository;
import service.BillingService;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * "Return Equipment" workflow.
 *
 * Lists every active rental (filtered to the current student if a
 * {@code userIdFilter} is supplied, or all rentals for staff), lets
 * the user pick one, enter a return date, pick a damage severity
 * tier, then click "Process Return". Behind the scenes this calls
 * {@link BillingService#processReturn}, which:
 *   1. flips the rental to RETURNED (and records the damage level),
 *   2. frees the equipment (back to AVAILABLE),
 *   3. generates and persists a bill.
 *
 * The bill breakdown is shown in a read-only summary below the form
 * so the staff (or student) can sanity-check the charge before they
 * hand the item back.
 */
public class ReturnPanel extends JPanel {

    /** Non-null -> only show this user's active rentals. */
    private final String userIdFilter;
    private final RentalRepository rentalRepo = new RentalRepository();
    private final BillingService billingService = new BillingService();

    private final JComboBox<Rental> rentalBox = new JComboBox<>();
    private final JSpinner returnDateSpinner;
    private final JComboBox<DamageLevel> damageBox =
        new JComboBox<>(DamageLevel.values());
    private final JButton returnButton = Theme.successButton("Process Return");
    private final JButton refreshButton = Theme.ghostButton("Refresh");
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.LEFT);
    private final JTextArea billPreview = new JTextArea(8, 40);

    /** Optional hook for the shell to refresh sibling panels after a
     *  successful return (My Rentals, Browse Equipment, etc.). */
    private Runnable onReturnProcessed;

    public ReturnPanel(String userIdFilter) {
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
        JLabel title = Theme.pageTitle("Return equipment");
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel sub   = Theme.subtitle("Pick the rental, set the return date, flag any damage, then process the return.");
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

        rentalBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Rental r) {
                    setText(String.format("#%d  -  %s  -  User %s  -  Due %s",
                        r.getRentalId(), r.getEquipmentId(),
                        r.getUserId(), r.getDueDate()));
                }
                return this;
            }
        });
        rentalBox.setFont(Theme.bodyFont());
        rentalBox.setBackground(Theme.CARD);
        rentalBox.setBorder(Theme.fieldBorder());
        rentalBox.setPreferredSize(new Dimension(360, 34));
        rentalBox.addActionListener(e -> updateBillPreview());

        returnDateSpinner = new JSpinner(new SpinnerDateModel());
        returnDateSpinner.setEditor(new JSpinner.DateEditor(returnDateSpinner, "yyyy-MM-dd"));
        returnDateSpinner.setValue(Date.from(LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        returnDateSpinner.addChangeListener(e -> updateBillPreview());

        damageBox.setFont(Theme.bodyFont());
        damageBox.setBackground(Theme.CARD);
        damageBox.setBorder(Theme.fieldBorder());
        damageBox.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    javax.swing.JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof DamageLevel d) {
                    setText(String.format("%s  -  RM %.2f fee", d.getDisplayName(), d.getFee()));
                }
                return this;
            }
        });
        damageBox.addActionListener(e -> updateBillPreview());

        returnButton.setPreferredSize(new Dimension(180, 38));
        refreshButton.setPreferredSize(new Dimension(120, 38));

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        form.add(formLabel("Active rental"), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(rentalBox, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(formLabel("Return date"), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(returnDateSpinner, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(formLabel("Damage status"), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        form.add(damageBox, c);

        c.gridx = 0; c.gridy = 3; c.weightx = 0; c.fill = GridBagConstraints.NONE;
        form.add(new JLabel(""), c);
        c.gridx = 1; c.weightx = 1.0; c.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(returnButton);
        buttons.add(refreshButton);
        form.add(buttons, c);

        card.add(form, BorderLayout.CENTER);

        billPreview.setEditable(false);
        billPreview.setLineWrap(true);
        billPreview.setWrapStyleWord(true);
        billPreview.setBackground(Theme.SURFACE);
        billPreview.setForeground(Theme.TEXT);
        billPreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1, true), "Bill preview"),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        billPreview.setFont(Theme.bodyFont());

        JPanel previewHolder = new JPanel(new BorderLayout());
        previewHolder.setOpaque(false);
        previewHolder.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        previewHolder.add(billPreview, BorderLayout.CENTER);
        card.add(previewHolder, BorderLayout.SOUTH);

        add(card, BorderLayout.NORTH);

        statusLabel.setFont(Theme.bodyFont());
        statusLabel.setBorder(new EmptyBorder(12, 4, 0, 4));
        add(statusLabel, BorderLayout.CENTER);

        returnButton.addActionListener(e -> onReturnClicked());
        refreshButton.addActionListener(e -> { reloadRentals(); updateBillPreview(); });

        reloadRentals();
        updateBillPreview();
    }

    private static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.bodyFont());
        l.setForeground(Theme.MUTED);
        return l;
    }

    /** Allow the shell to give us a hook that fires after each
     *  successful return so siblings can refresh. */
    public void setOnReturnProcessed(Runnable r) {
        this.onReturnProcessed = r;
    }

    /** Public so the shell can force a refresh after a new rental. */
    public void reloadRentals() {
        List<Rental> all;
        try {
            all = rentalRepo.findAll();
        } catch (Exception ex) {
            GuiUtil.showError(this, "Failed to load rentals",
                "Could not load rentals:\n" + ex.getMessage());
            return;
        }
        Vector<Rental> active = new Vector<>();
        for (Rental r : all) {
            if (r.getStatus() != Rental.Status.ACTIVE
                && r.getStatus() != Rental.Status.OVERDUE) continue;
            if (userIdFilter != null && !userIdFilter.equals(r.getUserId())) continue;
            active.add(r);
        }
        rentalBox.setModel(new DefaultComboBoxModel<>(active));
        if (active.isEmpty()) {
            statusLabel.setForeground(Theme.MUTED);
            statusLabel.setText(userIdFilter == null
                ? "No active rentals in the system."
                : "You have no active rentals. Rent something first!");
            returnButton.setEnabled(false);
            damageBox.setEnabled(false);
            returnDateSpinner.setEnabled(false);
        } else {
            statusLabel.setText(" ");
            returnButton.setEnabled(true);
            damageBox.setEnabled(true);
            returnDateSpinner.setEnabled(true);
        }
    }

    /** Pure preview calculation - no DB writes. Mirrors BillingService
     *  closely so the displayed numbers match the persisted bill. */
    private void updateBillPreview() {
        Rental r = (Rental) rentalBox.getSelectedItem();
        if (r == null) {
            billPreview.setText("Select a rental to preview the bill.");
            return;
        }
        LocalDate returnDate = toLocalDate((Date) returnDateSpinner.getValue());
        DamageLevel dmg = (DamageLevel) damageBox.getSelectedItem();
        try {
            model.user.User u = new UserRepository().findById(r.getUserId()).orElse(null);
            EquipmentItem item = new EquipmentRepository().findAll().stream()
                .filter(e -> e.getEquipmentId().equals(r.getEquipmentId())).findFirst().orElse(null);
            if (u == null || item == null) {
                billPreview.setText("Cannot resolve user/equipment for this rental.");
                return;
            }
            Bill bill = billingService.calculateBill(u, item, r, returnDate, dmg);
            int days = r.rentalDays();
            int daysLate = r.daysLate(returnDate);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Rental #%d  -  %s  -  %d day(s) total%n",
                r.getRentalId(), r.getEquipmentId(), days));
            sb.append(String.format("Return on %s", returnDate));
            if (daysLate > 0) sb.append(String.format("  -  %d day(s) LATE", daysLate));
            sb.append(String.format("  -  damage: %s%n", dmg.getDisplayName()));
            sb.append('\n');
            sb.append(String.format("Base fee            : RM %8.2f%n", bill.getBaseRentalFee()));
            sb.append(String.format("Late/damage penalty : RM %8.2f%n", bill.getPenaltyAmount()));
            sb.append(String.format("Discount            : RM %8.2f%n", -bill.getDiscountAmount()));
            sb.append(String.format("Net payable         : RM %8.2f%n", bill.getNetPayable()));
            billPreview.setText(sb.toString());
        } catch (Exception ex) {
            billPreview.setText("Bill preview unavailable: " + ex.getMessage());
        }
    }

    private void onReturnClicked() {
        Rental r = (Rental) rentalBox.getSelectedItem();
        if (r == null) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText("Please choose a rental to return.");
            return;
        }
        LocalDate returnDate = toLocalDate((Date) returnDateSpinner.getValue());
        DamageLevel dmg = (DamageLevel) damageBox.getSelectedItem();

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Process return for rental #%d on %s?%nDamage: %s (RM %.2f fee)",
                r.getRentalId(), returnDate, dmg.getDisplayName(), dmg.getFee()),
            "Confirm return",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) return;

        try {
            Bill bill = billingService.processReturn(
                r.getRentalId(), returnDate, dmg);
            statusLabel.setForeground(Theme.SUCCESS);
            statusLabel.setText(String.format(
                "Return processed. Net payable: RM %.2f.", bill.getNetPayable()));
            reloadRentals();
            updateBillPreview();
            if (onReturnProcessed != null) {
                try { onReturnProcessed.run(); } catch (Exception ignored) {}
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            statusLabel.setForeground(Theme.DANGER);
            statusLabel.setText(ex.getMessage());
        } catch (Exception ex) {
            GuiUtil.showError(this, "Return failed",
                "Could not process return:\n" + ex.getMessage());
        }
    }

    private static LocalDate toLocalDate(Date d) {
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}