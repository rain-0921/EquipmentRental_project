package model.equipment;

import model.pricing.PricingPolicy;

/**
 * Builds the correct {@link EquipmentItem} subclass from a category
 * string coming out of the database. Hides the choice from the rest
 * of the system so new categories can be added by editing this file
 * + adding the new enum value + adding the new refinement class.
 */
public final class EquipmentItemFactory {

    private EquipmentItemFactory() { /* static helper */ }

    public static EquipmentItem create(String equipmentId,
                                       String name,
                                       EquipmentCategory category,
                                       double dailyRate,
                                       PricingPolicy policy,
                                       EquipmentStatus status) {
        return switch (category) {
            case ELECTRONICS -> new ElectronicsItem(equipmentId, name, dailyRate, policy, status);
            case MEDIA       -> new MediaItem(equipmentId, name, dailyRate, policy, status);
            case LABORATORY  -> new LaboratoryItem(equipmentId, name, dailyRate, policy, status);
        };
    }
}