package rental.model.equipment;

import rental.model.pricing.PricingPolicy;
import rental.model.penalty.PenaltyRule;

public class Electronics extends Equipment {
    public Electronics(String equipmentId, String name, String description,
                       double dailyRate, PricingPolicy pricingPolicy, PenaltyRule penaltyRule) {
        super(equipmentId, name, description, EquipmentCategory.ELECTRONICS,
              dailyRate, pricingPolicy, penaltyRule);
    }
}
