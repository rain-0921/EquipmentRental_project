package model.equipment;

import model.pricing.PricingPolicy;

/** Concrete refinement for the Electronics category. */
public class ElectronicsItem extends EquipmentItem {

    /** Electronics get a 0.5x category late multiplier - low value. */
    private static final double LATE_MULTIPLIER = 0.5;

    public ElectronicsItem(String id, String name, double dailyRate,
                           PricingPolicy policy, EquipmentStatus status) {
        super(id, name, EquipmentCategory.ELECTRONICS, dailyRate, policy, status);
    }

    @Override
    public double getCategoryLatePenaltyMultiplier() {
        return LATE_MULTIPLIER;
    }

    @Override
    public String getCategoryDisplayName() {
        return "Electronics";
    }
}