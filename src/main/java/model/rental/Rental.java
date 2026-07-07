package model.rental;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A single rental transaction. Composed of a {@link model.user.User}
 * and an {@link model.equipment.EquipmentItem} - both looked up by id
 * and resolved by the repository at read time. Keeps this class a
 * pure data holder so it can be reconstructed cheaply from JDBC.
 */
public class Rental {

    public enum Status { ACTIVE, RETURNED, OVERDUE, CANCELLED }

    /**
     * How badly the item was returned, if at all. Drives the
     * damage fee charged on top of any late penalty.
     *
     *   NONE     - no damage             RM    0
     *   LIGHT    - minor scuff/scratch   RM   10
     *   MODERATE - functional impact     RM  100
     *   HEAVY    - significant damage    RM 1000
     */
    public enum DamageLevel {
        NONE("None",       0.0),
        LIGHT("Light",    10.0),
        MODERATE("Moderate", 100.0),
        HEAVY("Heavy",  1000.0);

        private final String displayName;
        private final double fee;

        DamageLevel(String displayName, double fee) {
            this.displayName = displayName;
            this.fee = fee;
        }

        public String getDisplayName() { return displayName; }
        public double getFee()         { return fee; }

        /** Parse a stored value, defaulting to NONE for null/unknown
         *  input so legacy rows don't blow up. */
        public static DamageLevel parse(String s) {
            if (s == null || s.isBlank()) return NONE;
            try { return DamageLevel.valueOf(s.trim().toUpperCase()); }
            catch (IllegalArgumentException ex) { return NONE; }
        }
    }

    private final int rentalId;
    private final String userId;
    private final String equipmentId;
    private final LocalDate rentDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private DamageLevel damageLevel;
    private Status status;

    public Rental(int rentalId,
                  String userId,
                  String equipmentId,
                  LocalDate rentDate,
                  LocalDate dueDate,
                  LocalDate returnDate,
                  DamageLevel damageLevel,
                  Status status) {
        this.rentalId = rentalId;
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.rentDate = rentDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.damageLevel = damageLevel == null ? DamageLevel.NONE : damageLevel;
        this.status = status;
    }

    public Rental(String userId,
                  String equipmentId,
                  LocalDate rentDate,
                  LocalDate dueDate) {
        this(0, userId, equipmentId, rentDate, dueDate, null,
             DamageLevel.NONE, Status.ACTIVE);
    }

    /** Number of rental days - always at least 1 to match the
     *  PricingPolicy contract. */
    public int rentalDays() {
        long days = ChronoUnit.DAYS.between(rentDate, dueDate) + 1;
        return (int) Math.max(1, days);
    }

    /** Days late as of the given {@code asOf} date. */
    public int daysLate(LocalDate asOf) {
        LocalDate effectiveReturn =
            returnDate != null ? returnDate : asOf;
        if (!effectiveReturn.isAfter(dueDate)) return 0;
        return (int) ChronoUnit.DAYS.between(dueDate, effectiveReturn);
    }

    // -- accessors ---------------------------------------------------
    public int getRentalId()                       { return rentalId; }
    public String getUserId()                      { return userId; }
    public String getEquipmentId()                 { return equipmentId; }
    public LocalDate getRentDate()                 { return rentDate; }
    public LocalDate getDueDate()                  { return dueDate; }
    public LocalDate getReturnDate()               { return returnDate; }
    public void setReturnDate(LocalDate d)         { this.returnDate = d; }
    public DamageLevel getDamageLevel()            { return damageLevel; }
    public void setDamageLevel(DamageLevel d)      { this.damageLevel = d == null ? DamageLevel.NONE : d; }
    /** Backwards-compat shim - returns true if any damage was flagged. */
    public boolean isDamaged()                     { return damageLevel != DamageLevel.NONE; }
    public void setDamaged(boolean d)              {
        this.damageLevel = d ? DamageLevel.MODERATE : DamageLevel.NONE;
    }
    public Status getStatus()                      { return status; }
    public void setStatus(Status s)                { this.status = s; }
}