package gui;

import java.time.LocalDate;

/**
 * Sanity checks for {@link DatePickerField}. Drives the field
 * programmatically (no popup needed) so it can run headless.
 */
public class DatePickerTest {

    private static boolean fail = false;
    private static void check(String name, boolean cond, String detail) {
        if (cond) System.out.println("  ok: " + name + (detail.isEmpty() ? "" : " (" + detail + ")"));
        else      { System.out.println("  FAIL: " + name + (detail.isEmpty() ? "" : " (" + detail + ")")); fail = true; }
    }

    public static void main(String[] args) {
        LocalDate today = LocalDate.now();

        // Initial: today, due today + 7
        DatePickerField rent = new DatePickerField(today);
        DatePickerField due  = new DatePickerField(today.plusDays(7));
        rent.setMinDate(today);
        due.setMinDate(today);

        check("Rent default = today", rent.getValue().equals(today),
            "actual=" + rent.getValue());
        check("Due default = today+7", due.getValue().equals(today.plusDays(7)),
            "actual=" + due.getValue());

        // Reject past value via setMinDate guard
        rent.setMinDate(today);
        rent.setValue(today.minusDays(3));
        check("setMinDate pushes past values forward",
            !rent.getValue().isBefore(today),
            "value after = " + rent.getValue());

        // Wire auto-bump: move rent to today+10, due should bump
        rent.addChangeListener(r -> {
            if (r == null) return;
            LocalDate d = due.getValue();
            if (d == null || d.isBefore(r)) due.setValue(r.plusDays(7));
            else                            due.setMinDate(r);
        });

        LocalDate newRent = today.plusDays(10);
        rent.setValue(newRent);
        check("Rent moved to today+10", rent.getValue().equals(newRent),
            "actual=" + rent.getValue());
        check("Due auto-bumped to today+17", due.getValue().equals(newRent.plusDays(7)),
            "actual=" + due.getValue());

        // Move rent back to today (still >= due min); due should stay valid
        rent.setValue(today);
        check("Due still today+17 after rent reset to today",
            due.getValue().equals(newRent.plusDays(7)),
            "actual=" + due.getValue());

        // Direct reject: try to set due before rent (manual override)
        boolean throwed = false;
        try {
            due.setMinDate(today);
            // setValue should accept but minDate clamp happens via setMinDate
            due.setValue(today.plusDays(1));
        } catch (Exception ex) {
            throwed = true;
        }
        check("setValue does not throw for valid future date", !throwed, "");

        System.out.println("---");
        System.out.println(fail ? "DatePickerTest: FAILURES" : "DatePickerTest: all checks passed");
        System.exit(fail ? 1 : 0);
    }
}