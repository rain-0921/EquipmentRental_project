package pricing;

import model.Equipment;

/**
 * Staff pricing: flat 20% discount on the base fee.
 * Late/damage penalty uses the shared formula in PricingStrategy.
 */
public class StaffDiscountPricingStrategy implements PricingStrategy {

    private static final double DISCOUNT_RATE = 0.20; // 20% staff discount

    @Override
    public double calculateBaseFee(Equipment equipment, int rentalDays) {
        return equipment.getDailyRentalRate() * rentalDays;
    }

    @Override
    public double calculateDiscount(Equipment equipment, double baseFee) {
        return baseFee * DISCOUNT_RATE;
    }

    @Override
    public String getStrategyName() {
        return "Staff Discount Pricing (20%)";
    }
}
