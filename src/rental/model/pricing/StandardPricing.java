package rental.model.pricing;

import rental.model.equipment.Equipment;

public class StandardPricing implements PricingPolicy {
    @Override
    public double computeRate(Equipment equipment, int days) {
        return equipment.getDailyRate() * days;
    }

    @Override
    public String planName() {
        return "Standard (0% off)";
    }
}
