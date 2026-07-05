package pricing;

import model.Equipment;

/**
 * Final-year (FYP) student pricing: 10% discount on the base fee.
 * Late/damage penalty uses the shared formula in PricingStrategy.
 */
public class FinalYearDiscountPricingStrategy implements PricingStrategy {

    private static final double DISCOUNT_RATE = 0.10; // 10% student (FYP) discount

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
        return "Final-Year Student Discount Pricing (10%)";
    }
}
