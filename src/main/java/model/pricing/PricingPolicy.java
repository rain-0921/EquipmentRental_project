package model.pricing;

/**
 * =====================================================================
 *  BRIDGE PATTERN - IMPLEMENTOR
 * =====================================================================
 *  {@code PricingPolicy} is the "implementor" side of the Bridge.
 *  It declares the operations that every pricing algorithm must
 *  provide, without coupling to any particular equipment category.
 *
 *  An {@link model.equipment.EquipmentItem EquipmentItem} (the
 *  abstraction) holds a reference to one of these and delegates the
 *  actual calculation to it. This lets us vary:
 *      - the equipment category   (Electronics / Media / Lab)
 *      - the pricing strategy     (Standard / Tiered / Promotional)
 *  independently, without an exponential subclass explosion.
 *
 *  See: README.md §"Bridge Pattern Justification"
 * =====================================================================
 */
public interface PricingPolicy {

    /**
     * Calculate the base rental fee for the given number of rental days.
     * Does NOT apply user discounts or late/damage penalties - those
     * are handled by the billing service.
     *
     * @param dailyRate  the equipment's published per-day rate (RM)
     * @param days       total rental duration in days (>= 1)
     * @return base rental fee in RM, never negative
     */
    double calculateBaseFee(double dailyRate, int days);

    /**
     * Calculate the late-return penalty for a rental that was returned
     * {@code daysLate} days past its due date. Returns 0 if returned
     * on time.
     */
    double calculateLatePenalty(double dailyRate, int daysLate);

    /**
     * Human-readable name for receipts and the GUI.
     */
    String getPolicyName();

    /**
     * Strategy key persisted in the {@code equipment.pricing_strategy}
     * column - keep these in sync with the DB ENUM.
     */
    String getStrategyKey();
}