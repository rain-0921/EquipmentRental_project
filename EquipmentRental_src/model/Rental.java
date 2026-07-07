package model;

import java.time.LocalDate;

/**
 * Represents one rental transaction. Demonstrates AGGREGATION/COMPOSITION:
 * a Rental is built from an existing Equipment and User (aggregation - they
 * exist independently), while a Rental "has-a" Bill once billed
 * (composition - the Bill's lifecycle belongs to the Rental).
 */
public class Rental {

    private static int idCounter = 1;

    private final String rentalId;
    private final Equipment equipment;
    private final User user;
    private final LocalDate rentDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private DamageLevel damageLevel = DamageLevel.NONE;
    private Bill bill;

    public Rental(Equipment equipment, User user, LocalDate rentDate, int plannedDays) {
        this.rentalId = "RNT-" + String.format("%04d", idCounter++);
        this.equipment = equipment;
        this.user = user;
        this.rentDate = rentDate;
        this.dueDate = rentDate.plusDays(plannedDays);
        this.equipment.setAvailable(false);

        // If the user is eligible for a discount, swap in the matching
        // pricing strategy via the Bridge — Equipment class itself is untouched.
        applyPricingForUser();
    }

    /**
     * Reconstruction constructor used when loading a persisted rental row
     * back from the database (keeps original ID/dates instead of regenerating them).
     */
    public Rental(String rentalId, Equipment equipment, User user, LocalDate rentDate,
                  LocalDate dueDate, LocalDate returnDate, DamageLevel damageLevel) {
        this.rentalId = rentalId;
        this.equipment = equipment;
        this.user = user;
        this.rentDate = rentDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.damageLevel = (damageLevel != null) ? damageLevel : DamageLevel.NONE;
        syncIdCounter(rentalId);
    }

    /**
     * Full reconstruction including the pricing strategy that was used when the
     * rental was originally created. Restores the correct strategy to the Equipment
     * so any subsequent operations (e.g. bill recalculation) use the right rules.
     */
    public static Rental fromPersistedRow(String rentalId, Equipment equipment, User user,
            LocalDate rentDate, LocalDate dueDate, LocalDate returnDate,
            DamageLevel damageLevel, String pricingStrategyName) {
        Rental r = new Rental(rentalId, equipment, user, rentDate, dueDate, returnDate, damageLevel);
        equipment.setPricingStrategy(pricing.PricingStrategy.fromName(pricingStrategyName));
        return r;
    }

    /** Attaches a Bill loaded from the database without recalculating it. */
    public void attachExistingBill(Bill bill) {
        this.bill = bill;
    }

    private static synchronized void syncIdCounter(String rentalId) {
        try {
            int n = Integer.parseInt(rentalId.replace("RNT-", ""));
            if (n >= idCounter) idCounter = n + 1;
        } catch (NumberFormatException ignored) { }
    }

    private void applyPricingForUser() {
        if (user instanceof Staff) {
            equipment.setPricingStrategy(new pricing.StaffDiscountPricingStrategy());
        } else if (user instanceof Student student && student.isFinalYear()) {
            equipment.setPricingStrategy(new pricing.FinalYearDiscountPricingStrategy());
        } else {
            equipment.setPricingStrategy(new pricing.StandardPricingStrategy());
        }
    }

    /** Marks the item as returned and generates the bill. */
    public Bill returnEquipment(LocalDate actualReturnDate, DamageLevel damageLevel) {
        this.returnDate = actualReturnDate;
        this.damageLevel = (damageLevel != null) ? damageLevel : DamageLevel.NONE;
        this.equipment.setAvailable(true);

        int rentalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(rentDate, dueDate);
        if (rentalDays <= 0) rentalDays = 1;

        int lateDays = (int) java.time.temporal.ChronoUnit.DAYS.between(dueDate, actualReturnDate);
        if (lateDays < 0) lateDays = 0;

        var strategy = equipment.getPricingStrategy();
        double baseFee = strategy.calculateBaseFee(equipment, rentalDays);
        double discount = strategy.calculateDiscount(equipment, baseFee);
        double penalty = strategy.calculatePenalty(equipment, lateDays, this.damageLevel);

        this.bill = new Bill(this, baseFee, discount, penalty, strategy.getStrategyName());
        return this.bill;
    }

    // ---- Getters ----
    public String getRentalId() { return rentalId; }
    public Equipment getEquipment() { return equipment; }
    public User getUser() { return user; }
    public LocalDate getRentDate() { return rentDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public DamageLevel getDamageLevel() { return damageLevel; }
    public boolean isDamagedOnReturn() { return damageLevel != null && damageLevel.isDamaged(); }
    public Bill getBill() { return bill; }

    @Override
    public String toString() {
        return String.format("%s | %s -> %s | Due: %s%s",
                rentalId, equipment.getName(), user.getFullName(), dueDate,
                returnDate != null ? " | Returned: " + returnDate : "");
    }
}
