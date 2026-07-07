package model.pricing;

/**
 * Volume-discount pricing:
 *   1-2 days    -> 100 % of daily rate per day
 *   3-6 days    -> 90 % per day
 *   7+ days     -> 80 % per day
 *
 * Late penalty uses an escalating multiplier so overdue longer
 * rentals hurt more (10 % extra per day late, capped at 100 %).
 */
public class TieredPricing implements PricingPolicy {

    private static final double TIER_1_MULTIPLIER = 1.00;
    private static final double TIER_2_MULTIPLIER = 0.90;
    private static final double TIER_3_MULTIPLIER = 0.80;
    private static final double PENALTY_BASE_RATE = 0.5;
    private static final double PENALTY_PER_DAY_ESCALATION = 0.10;
    private static final double PENALTY_CAP = 1.00;

    @Override
    public double calculateBaseFee(double dailyRate, int days) {
        if (days <= 0) return 0.0;
        double total = 0.0;
        for (int day = 1; day <= days; day++) {
            total += dailyRate * multiplierForDay(day);
        }
        return round2(total);
    }

    @Override
    public double calculateLatePenalty(double dailyRate, int daysLate) {
        if (daysLate <= 0) return 0.0;
        double multiplier = Math.min(
            PENALTY_BASE_RATE + PENALTY_PER_DAY_ESCALATION * daysLate,
            PENALTY_CAP);
        return round2(dailyRate * multiplier * daysLate);
    }

    @Override
    public String getPolicyName() {
        return "Tiered Pricing (volume discount)";
    }

    @Override
    public String getStrategyKey() {
        return "TIERED";
    }

    private double multiplierForDay(int day) {
        if (day <= 2) return TIER_1_MULTIPLIER;
        if (day <= 6) return TIER_2_MULTIPLIER;
        return TIER_3_MULTIPLIER;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}