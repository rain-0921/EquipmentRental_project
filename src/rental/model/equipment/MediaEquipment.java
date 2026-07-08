package rental.model.equipment;

import rental.model.pricing.PricingPolicy;
import rental.model.penalty.PenaltyRule;

public class MediaEquipment extends Equipment {
    public MediaEquipment(String equipmentId, String name, String description,
                          double dailyRate, PricingPolicy pricingPolicy, PenaltyRule penaltyRule) {
        super(equipmentId, name, description, EquipmentCategory.MEDIA,
              dailyRate, pricingPolicy, penaltyRule);
    }
}
