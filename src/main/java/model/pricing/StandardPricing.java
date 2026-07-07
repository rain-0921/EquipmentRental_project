package model.pricing;

/**
 * Plain linear pricing: fee = dailyRate * days.
 * Late penalty = 50 % of the daily rate per day late.
 */
public class StandardPricing implements PricingPolicy {

    private static final double LATE_PENALTY_MULTIPLIER = 0.5;

    @Override
    public double calculateBaseFee(double dailyRate, int days) {
        if (days <= 0) return 0.0;
        return round2(dailyRate * days);
    }

    @Override
    public double calculateLatePenalty(double dailyRate, int daysLate) {
        if (daysLate <= 0) return 0.0;
        return round2(dailyRate * LATE_PENALTY_MULTIPLIER * daysLate);
    }

    @Override
    public String getPolicyName() {
        return "Standard Pricing";
    }

    @Override
    public String getStrategyKey() {
        return "STANDARD";
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}