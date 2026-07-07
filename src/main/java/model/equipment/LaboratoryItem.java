package model.equipment;

import model.pricing.PricingPolicy;

/** Concrete refinement for Laboratory Equipment - highest penalty
 *  because lab gear is expensive and often in short supply. */
public class LaboratoryItem extends EquipmentItem {

    /** Lab gear is high-value, so 1.0x multiplier on top of pricing. */
    private static final double LATE_MULTIPLIER = 1.0;

    public LaboratoryItem(String id, String name, double dailyRate,
                          PricingPolicy policy, EquipmentStatus status) {
        super(id, name, EquipmentCategory.LABORATORY, dailyRate, policy, status);
    }

    @Override
    public double getCategoryLatePenaltyMultiplier() {
        return LATE_MULTIPLIER;
    }

    @Override
    public String getCategoryDisplayName() {
        return "Laboratory Equipment";
    }
}