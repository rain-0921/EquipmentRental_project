package rental.model.pricing;

import rental.model.equipment.Equipment;

public class FinalYearStudentDiscountPricing implements PricingPolicy {
    @Override
    public double computeRate(Equipment equipment, int days) {
        return equipment.getDailyRate() * days;
    }

    @Override
    public String planName() {
        return "FYP Discount (15% off total)";
    }
}
