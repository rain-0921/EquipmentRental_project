package pricing;

import model.Equipment;

/** Default pricing: no discount. Late/damage penalty uses the shared formula in PricingStrategy. */
public class StandardPricingStrategy implements PricingStrategy {

    @Override
    public double calculateBaseFee(Equipment equipment, int rentalDays) {
        return equipment.getDailyRentalRate() * rentalDays;
    }

    @Override
    public double calculateDiscount(Equipment equipment, double baseFee) {
        return 0.0;
    }

    @Override
    public String getStrategyName() {
        return "Standard Pricing";
    }
}
