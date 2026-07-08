package rental.model.pricing;

import rental.model.equipment.Equipment;

public class StaffPricing implements PricingPolicy {
    @Override
    public double computeRate(Equipment equipment, int days) {
        return equipment.getDailyRate() * days;
    }

    @Override
    public String planName() {
        return "Staff Discount (20% off final)";
    }
}
