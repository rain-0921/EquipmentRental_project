package model.pricing;

/**
 * Promotional pricing: 15 % off the base rental, but the late
 * penalty is doubled (still capped at 100 % of one full day-rate
 * per overdue day) to discourage abuse of the discount.
 */
public class PromotionalPricing implements PricingPolicy {

    private static final double PROMO_DISCOUNT = 0.15;
    private static final double LATE_PENALTY_BASE = 1.00;   // 100 % of daily rate per day late
    private static final double LATE_PENALTY_CAP  = 2.00;

    @Override
    public double calculateBaseFee(double dailyRate, int days) {
        if (days <= 0) return 0.0;
        double gross = dailyRate * days;
        return round2(gross * (1.0 - PROMO_DISCOUNT));
    }

    @Override
    public double calculateLatePenalty(double dailyRate, int daysLate) {
        if (daysLate <= 0) return 0.0;
        double multiplier = Math.min(LATE_PENALTY_BASE, LATE_PENALTY_CAP);
        // Promo abuse: every overdue day = 100 % of daily rate.
        return round2(dailyRate * multiplier * daysLate);
    }

    @Override
    public String getPolicyName() {
        return "Promotional Pricing (15% off)";
    }

    @Override
    public String getStrategyKey() {
        return "PROMOTIONAL";
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}