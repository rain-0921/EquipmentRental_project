package rental.model.equipment;

import rental.model.pricing.PricingPolicy;
import rental.model.penalty.PenaltyRule;

public class LaboratoryEquipment extends Equipment {
    public LaboratoryEquipment(String equipmentId, String name, String description,
                               double dailyRate, PricingPolicy pricingPolicy, PenaltyRule penaltyRule) {
        super(equipmentId, name, description, EquipmentCategory.LABORATORY,
              dailyRate, pricingPolicy, penaltyRule);
    }
}
