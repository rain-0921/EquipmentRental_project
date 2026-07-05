package model;

import pricing.PricingStrategy;
import pricing.StandardPricingStrategy;

/**
 * Abstract base class for all rentable equipment.
 * Demonstrates ABSTRACTION (abstract methods each subcategory must define)
 * and is the base of an INHERITANCE hierarchy (Electronics, MediaEquipment,
 * LabEquipment).
 *
 * BRIDGE PATTERN:
 * Equipment is the "Abstraction" side of the Bridge. Instead of hard-coding
 * pricing/penalty rules per subclass, each Equipment instance holds a
 * reference to a PricingStrategy ("Implementor"). This decouples *what kind
 * of equipment it is* from *how it is priced*, so new pricing/penalty rules
 * can be added later without touching the Equipment class hierarchy at all.
 */
public abstract class Equipment {

    private static int idCounter = 1000;

    private final String equipmentId;
    private String name;
    private double dailyRentalRate;
    private boolean available;

    // Bridge: the pricing "implementor" this equipment currently uses.
    protected PricingStrategy pricingStrategy;

    protected Equipment(String name, double dailyRentalRate) {
        this.equipmentId = "EQ-" + (idCounter++);
        this.name = name;
        this.dailyRentalRate = dailyRentalRate;
        this.available = true;
        // Sensible default; can be swapped at runtime (that's the point of Bridge).
        this.pricingStrategy = new StandardPricingStrategy();
    }

    /**
     * Reconstruction constructor used when loading a persisted row back
     * from the database (keeps the original ID instead of generating a new one).
     */
    protected Equipment(String equipmentId, String name, double dailyRentalRate, boolean available) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.dailyRentalRate = dailyRentalRate;
        this.available = available;
        this.pricingStrategy = new StandardPricingStrategy();
        syncIdCounter(equipmentId);
    }

    private static synchronized void syncIdCounter(String equipmentId) {
        try {
            int n = Integer.parseInt(equipmentId.replace("EQ-", ""));
            if (n >= idCounter) idCounter = n + 1;
        } catch (NumberFormatException ignored) { }
    }

    // ---- Abstract behaviour every concrete category must supply ----

    /** Human-readable category name, e.g. "Electronics". */
    public abstract String getCategory();

    /** Category-specific late-return penalty rate applied per day late. */
    public abstract double getLatePenaltyRatePerDay();

    // ---- Bridge accessor ----

    public void setPricingStrategy(PricingStrategy strategy) {
        this.pricingStrategy = strategy;
    }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    // ---- Standard getters/setters ----

    public String getEquipmentId() { return equipmentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getDailyRentalRate() { return dailyRentalRate; }
    public void setDailyRentalRate(double dailyRentalRate) { this.dailyRentalRate = dailyRentalRate; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - RM%.2f/day - %s",
                equipmentId, name, getCategory(), dailyRentalRate,
                available ? "Available" : "Rented Out");
    }
}
