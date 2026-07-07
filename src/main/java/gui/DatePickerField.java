package gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Popup calendar date picker. Two parts: a styled text field that
 * shows the picked date and opens the calendar when clicked, and the
 * calendar itself (a small month-grid popup).
 *
 * <ul>
 *   <li>Past dates (before {@link #setMinDate}) appear disabled.</li>
 *   <li>Type into the field to enter a date by hand.</li>
 *   <li>"Today" and "Clear" shortcuts appear inside the popup.</li>
 * </ul>
 */
public class DatePickerField extends JPanel {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DISPLAY =
        DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH);

    private LocalDate value;
    private LocalDate minDate = LocalDate.now();

    private final JTextField field = new JTextField();
    private final JButton    button = new JButton("Pick");
    private final JLabel     hint = new JLabel(" ");

    private Popup popup;
    private YearMonth displayMonth;

    /** Hint label exposed so the parent layout can render it below
     *  the field (e.g. "Selected: Mon, 8 Jul 2026"). */
    public JLabel getHintComponent() { return hint; }

    public DatePickerField(LocalDate initial) {
        super(new BorderLayout(8, 0));
        setOpaque(false);
        this.value = initial != null ? initial : LocalDate.now();
        this.displayMonth = YearMonth.from(this.value);

        field.setFont(Theme.bodyFont());
        field.setBorder(Theme.fieldBorder());
        field.setPreferredSize(new Dimension(180, 34));
        field.setBackground(Theme.CARD);
        field.setForeground(Theme.TEXT);
        field.addActionListener(e -> commitTypedDate());
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { commitTypedDate(); }
        });
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { syncHint(); }
            @Override public void removeUpdate(DocumentEvent e)  { syncHint(); }
            @Override public void changedUpdate(DocumentEvent e) { syncHint(); }
        });

        button.setFont(Theme.buttonFont());
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setForeground(Color.WHITE);
        button.setBackground(Theme.ACCENT);
        button.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        button.setText("\uD83D\uDCC5"); // calendar glyph
        button.setPreferredSize(new Dimension(46, 34));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.addActionListener(e -> showPopup());

        add(field, BorderLayout.CENTER);
        add(button, BorderLayout.EAST);

        hint.setFont(Theme.smallFont());
        hint.setForeground(Theme.DANGER);
        hint.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 0));

        syncDisplay();
    }

    public void setHint(String text) {
        hint.setText(text != null ? text : " ");
        hint.setForeground(Theme.MUTED);
    }

    /** Earliest date allowed. Null = no minimum. */
    public void setMinDate(LocalDate min) {
        this.minDate = min;
        if (min != null && value != null && value.isBefore(min)) {
            value = min;
            syncDisplay();
            fireChange();
        }
    }

    public LocalDate getValue() { return value; }

    public void setValue(LocalDate v) {
        if (v != null && minDate != null && v.isBefore(minDate)) {
            v = minDate;
        }
        this.value = v;
        if (v != null) this.displayMonth = YearMonth.from(v);
        syncDisplay();
        fireChange();
    }

    /** Listener notified when the date changes through user input. */
    public void addChangeListener(java.util.function.Consumer<LocalDate> l) {
        listeners.add(l);
    }

    private final java.util.List<java.util.function.Consumer<LocalDate>> listeners =
        new java.util.ArrayList<>();
    private void fireChange() {
        for (java.util.function.Consumer<LocalDate> l : listeners) l.accept(value);
    }

    // --- popup ----------------------------------------------------------

    private void showPopup() {
        hidePopup();
        JPanel cal = buildCalendarPanel();

        Point p = field.getLocationOnScreen();
        SwingUtilities.convertPointFromScreen(p, this);
        PopupFactory pf = PopupFactory.getSharedInstance();
        popup = pf.getPopup(this, cal,
            p.x, p.y + field.getHeight() + 4);
        popup.show();
        field.requestFocusInWindow();
    }

    private void hidePopup() {
        if (popup != null) {
            popup.hide();
            popup = null;
        }
    }

    private JPanel buildCalendarPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.CARD);
        root.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        // Title + nav
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JLabel monthLabel = new JLabel(
            displayMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            + " " + displayMonth.getYear(),
            javax.swing.SwingConstants.CENTER);
        monthLabel.setFont(Theme.h2Font());
        monthLabel.setForeground(Theme.TEXT);
        head.add(monthLabel, BorderLayout.CENTER);

        JButton prev = new JButton("\u25C0");
        JButton next = new JButton("\u25B6");
        styleMiniButton(prev);
        styleMiniButton(next);
        prev.addActionListener(e -> shiftMonth(-1));
        next.addActionListener(e -> shiftMonth(+1));
        head.add(prev, BorderLayout.WEST);
        head.add(next, BorderLayout.EAST);
        root.add(head, BorderLayout.NORTH);

        // Day-of-week header
        JPanel dow = new JPanel(new GridLayout(1, 7, 4, 4));
        dow.setOpaque(false);
        dow.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        String[] dows = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : dows) {
            JLabel l = new JLabel(d, javax.swing.SwingConstants.CENTER);
            l.setFont(Theme.smallFont());
            l.setForeground(Theme.MUTED);
            dow.add(l);
        }
        root.add(dow, BorderLayout.CENTER);

        // Grid
        JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));
        grid.setOpaque(false);
        LocalDate first = displayMonth.atDay(1);
        int firstDow = first.getDayOfWeek().getValue() % 7; // Sun=0
        YearMonth prevMonth = displayMonth.minusMonths(1);
        int prevLen = prevMonth.lengthOfMonth();
        for (int i = 0; i < firstDow; i++) {
            int day = prevLen - firstDow + 1 + i;
            grid.add(makeCell(LocalDate.of(prevMonth.getYear(), prevMonth.getMonth(), day), true, false, false));
        }
        int days = displayMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();
        for (int d = 1; d <= days; d++) {
            LocalDate cell = displayMonth.atDay(d);
            boolean disabled = minDate != null && cell.isBefore(minDate);
            boolean isSelected = value != null && cell.equals(value);
            boolean isToday = cell.equals(today);
            grid.add(makeCell(cell, false, disabled, isSelected || isToday));
        }
        while (grid.getComponentCount() % 7 != 0) {
            int day = grid.getComponentCount() - firstDow - days + 1;
            LocalDate cell = displayMonth.plusMonths(1).atDay(Math.max(1, day));
            grid.add(makeCell(cell, true, true, false));
        }
        // pad to 6 rows for nicer height
        while (grid.getComponentCount() < 42) {
            int leftover = grid.getComponentCount();
            int day = leftover - firstDow - days + 1;
            LocalDate cell = displayMonth.plusMonths(1).atDay(Math.max(1, day));
            grid.add(makeCell(cell, true, true, false));
        }
        root.add(grid, BorderLayout.SOUTH);

        // Actions
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        actions.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        JButton todayBtn = new JButton("Today");
        JButton clearBtn = new JButton("Clear");
        styleMiniButton(todayBtn);
        styleMiniButton(clearBtn);
        todayBtn.addActionListener(e -> { setValue(LocalDate.now()); hidePopup(); });
        clearBtn.addActionListener(e -> { setValue(null);                hidePopup(); });
        actions.add(clearBtn);
        actions.add(todayBtn);
        JPanel withActions = new JPanel(new BorderLayout());
        withActions.setOpaque(false);
        withActions.add(root, BorderLayout.CENTER);
        withActions.add(actions, BorderLayout.SOUTH);

        withActions.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JComponent inner = forFocusLoss(withActions);
        JPanel finalWrap = wrap(inner);
        return finalWrap;
    }

    /** Wrap in a panel with inset so the popup isn't tight against the field. */
    private JPanel wrap(JComponent inner) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(inner, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return p;
    }

    /** Hide popup when this component loses focus. */
    private JComponent forFocusLoss(JComponent c) {
        c.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                javax.swing.Timer t = new javax.swing.Timer(120, (ActionEvent ev) -> {
                    if (popup != null) {
                        java.awt.Component focus = javax.swing.FocusManager.getCurrentManager().getFocusOwner();
                        boolean focusInsidePopup = focus != null
                            && javax.swing.SwingUtilities.isDescendingFrom(focus, c);
                        if (!focusInsidePopup) hidePopup();
                    }
                });
                t.setRepeats(false);
                t.start();
            }
        });
        return c;
    }

    private JButton makeCell(LocalDate d, boolean otherMonth, boolean disabled, boolean selected) {
        JButton b = new JButton(String.valueOf(d.getDayOfMonth()));
        b.setFont(Theme.bodyFont());
        b.setFocusPainted(false);
        b.setMargin(new Insets(2, 2, 2, 2));
        b.setBorder(BorderFactory.createLineBorder(
            selected ? Theme.ACCENT : new Color(0, 0, 0, 0), 1, true));
        b.setBackground(selected ? Theme.ACCENT_SOFT : Theme.CARD);
        b.setForeground(disabled ? Theme.DISABLED_BG : (otherMonth ? Theme.MUTED : Theme.TEXT));
        b.setEnabled(!disabled);
        b.setCursor(disabled
            ? java.awt.Cursor.getDefaultCursor()
            : java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        b.addActionListener(e -> { setValue(d); hidePopup(); });
        return b;
    }

    private void shiftMonth(int delta) {
        displayMonth = displayMonth.plusMonths(delta);
        // rebuild popup by hiding & re-showing
        hidePopup();
        showPopup();
    }

    private void styleMiniButton(JButton b) {
        b.setFont(Theme.buttonFont());
        b.setFocusPainted(false);
        b.setBackground(Theme.SURFACE);
        b.setForeground(Theme.TEXT);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        b.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }

    // --- typing -------------------------------------------------------

    private void commitTypedDate() {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            value = null;
            syncDisplay();
            fireChange();
            return;
        }
        try {
            LocalDate parsed = LocalDate.parse(text, ISO);
            if (minDate != null && parsed.isBefore(minDate)) {
                hint.setText("Date " + parsed.format(DISPLAY) + " is before today (" +
                    minDate.format(DISPLAY) + ")");
                hint.setForeground(Theme.DANGER);
                return;
            }
            value = parsed;
            displayMonth = YearMonth.from(parsed);
            syncDisplay();
            fireChange();
        } catch (DateTimeParseException ex) {
            hint.setText("Use YYYY-MM-DD, e.g. " + LocalDate.now().format(ISO));
            hint.setForeground(Theme.DANGER);
        }
    }

    private void syncDisplay() {
        field.setText(value == null ? "" : value.format(ISO));
        syncHint();
    }

    private void syncHint() {
        try {
            if (field.getText().isBlank()) { hint.setText(" "); return; }
            LocalDate.parse(field.getText().trim(), ISO);
            hint.setText(value != null ? "Selected: " + value.format(DISPLAY) : " ");
            hint.setForeground(Theme.MUTED);
        } catch (DateTimeParseException ex) {
            hint.setText("Invalid date format - use YYYY-MM-DD");
            hint.setForeground(Theme.DANGER);
        }
    }

    /** Make sure this component lays out properly when used in a
     *  vertical BoxLayout / GridBagLayout. */
    @Override public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, 34);
        return d;
    }
}