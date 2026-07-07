package pricing;

import model.DamageLevel;
import model.Equipment;

/**
 * BRIDGE PATTERN - "Implementor" interface.
 *
 * Defines the pricing/discount/penalty computation contract independently
 * of the Equipment class hierarchy. Any Equipment subclass can be paired
 * with any PricingStrategy at runtime (Equipment.setPricingStrategy),
 * so new pricing schemes can be added later WITHOUT modifying Equipment,
 * Electronics, MediaEquipment, LabEquipment, or any existing strategy.
 */
public interface PricingStrategy {

    /** Base rental fee before discounts/penalties, for the given rental days. */
    double calculateBaseFee(Equipment equipment, int rentalDays);

    /** Discount amount (positive number) to subtract from the base fee. */
    double calculateDiscount(Equipment equipment, double baseFee);

    /**
     * Penalty amount (positive number) combining the late-return surcharge and
     * the damage surcharge. Shared by every strategy so late/damage rules stay
     * consistent regardless of who is renting; only the discount differs
     * between Standard / Final-Year / Staff pricing.
     *
     * Late surcharge: lateDays x the equipment category's own late-penalty rate
     * (Electronics RM15/day, Media RM10/day, Lab RM20/day - supplied by each
     * Equipment subclass via getLatePenaltyRatePerDay()). This lets every
     * category apply its own penalty rule without changing this strategy.
     *
     * Damage surcharge: dailyRate x damage-level multiplier
     * (Small/小 x1, Medium/中 x2, Large/大 x4, None = 0).
     */
    default double calculatePenalty(Equipment equipment, int lateDays, DamageLevel damageLevel) {
        double rate = equipment.getDailyRentalRate();
        double latePenalty = 0.0;
        if (lateDays > 0) {
            latePenalty = lateDays * equipment.getLatePenaltyRatePerDay();
        }
        double damagePenalty = (damageLevel == null) ? 0.0 : rate * damageLevel.getMultiplier();
        return latePenalty + damagePenalty;
    }

    /** Label shown on the bill, e.g. "Standard Pricing", "Staff Discount Pricing". */
    String getStrategyName();

    /** Reconstructs the correct strategy instance from its persisted name string. */
    static PricingStrategy fromName(String name) {
        if (name == null) return new StandardPricingStrategy();
        if (name.contains("Staff")) return new StaffDiscountPricingStrategy();
        if (name.contains("Final")) return new FinalYearDiscountPricingStrategy();
        return new StandardPricingStrategy();
    }
}
