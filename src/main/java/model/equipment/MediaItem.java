package model.equipment;

import model.pricing.PricingPolicy;

/** Concrete refinement for Media Equipment. */
public class MediaItem extends EquipmentItem {

    /** Media equipment has a moderate 0.75x category late multiplier. */
    private static final double LATE_MULTIPLIER = 0.75;

    public MediaItem(String id, String name, double dailyRate,
                     PricingPolicy policy, EquipmentStatus status) {
        super(id, name, EquipmentCategory.MEDIA, dailyRate, policy, status);
    }

    @Override
    public double getCategoryLatePenaltyMultiplier() {
        return LATE_MULTIPLIER;
    }

    @Override
    public String getCategoryDisplayName() {
        return "Media Equipment";
    }
}