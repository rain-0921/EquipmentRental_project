package model.equipment;

import model.pricing.PricingPolicy;

import java.util.Objects;

/**
 * =====================================================================
 *  BRIDGE PATTERN - ABSTRACTION
 * =====================================================================
 *  {@code EquipmentItem} is the "abstraction" side of the Bridge.
 *
 *  It holds a reference to a {@link PricingPolicy} implementor and
 *  delegates the actual fee / penalty maths to it. Because the
 *  policy is injected (constructor), the category hierarchy
 *  (Electronics / Media / Laboratory) and the pricing hierarchy
 *  (Standard / Tiered / Promotional) can vary independently:
 *
 *      ElectronicsLaptop  ---> TieredPricing
 *      MediaProjector     ---> StandardPricing
 *      LabOscilloscope    ---> PromotionalPricing
 *      ... and any future combination.
 *
 *  Without the Bridge, each new combination would force a new
 *  concrete subclass (M*N problem). With the Bridge, M+N classes
 *  cover all combinations.
 * =====================================================================
 */
public abstract class EquipmentItem {

    private final String equipmentId;
    private final String name;
    private final EquipmentCategory category;
    private final double dailyRate;
    private PricingPolicy pricingPolicy;
    private EquipmentStatus status;

    protected EquipmentItem(String equipmentId,
                            String name,
                            EquipmentCategory category,
                            double dailyRate,
                            PricingPolicy pricingPolicy,
                            EquipmentStatus status) {
        this.equipmentId = Objects.requireNonNull(equipmentId, "equipmentId");
        this.name        = Objects.requireNonNull(name, "name");
        this.category    = Objects.requireNonNull(category, "category");
        if (dailyRate < 0) throw new IllegalArgumentException("dailyRate < 0");
        this.dailyRate   = dailyRate;
        this.pricingPolicy = Objects.requireNonNull(pricingPolicy, "pricingPolicy");
        this.status      = Objects.requireNonNull(status, "status");
    }

    /** Each category exposes its own late-return multiplier so the
     *  billing service can layer category-specific penalties on top
     *  of whatever the pricing strategy produced. */
    public abstract double getCategoryLatePenaltyMultiplier();

    /** Display name of the category (e.g. "Electronics"). */
    public abstract String getCategoryDisplayName();

    /** Convenience: delegates to the bridged pricing policy. */
    public double calculateBaseFee(int days) {
        return pricingPolicy.calculateBaseFee(dailyRate, days);
    }

    /** Convenience: delegates to the bridged pricing policy. */
    public double calculateLatePenalty(int daysLate) {
        return pricingPolicy.calculateLatePenalty(dailyRate, daysLate);
    }

    /** Swap the pricing strategy at runtime (admin use case). */
    public void setPricingPolicy(PricingPolicy newPolicy) {
        this.pricingPolicy = Objects.requireNonNull(newPolicy, "newPolicy");
    }

    // -- accessors -----------------------------------------------------
    public String getEquipmentId()                  { return equipmentId; }
    public String getName()                         { return name; }
    public EquipmentCategory getCategory()          { return category; }
    public double getDailyRate()                    { return dailyRate; }
    public PricingPolicy getPricingPolicy()         { return pricingPolicy; }
    public EquipmentStatus getStatus()              { return status; }
    public void setStatus(EquipmentStatus status)   { this.status = status; }

    public boolean isAvailable() {
        return status == EquipmentStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return String.format("%s [%s] - %s @ RM %.2f/day (%s)",
            equipmentId, name, category.getDisplayName(), dailyRate,
            pricingPolicy.getPolicyName());
    }
}