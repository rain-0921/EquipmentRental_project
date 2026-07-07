package model;

/**
 * Represents the condition an item is returned in.
 * 损坏程度 (damage severity): each level charges a surcharge equal to the
 * equipment's daily rental rate multiplied by a fixed factor:
 *   NONE   - no damage, no surcharge
 *   SMALL  (小) - daily rate x 1
 *   MEDIUM (中) - daily rate x 2
 *   LARGE  (大) - daily rate x 4
 */
public enum DamageLevel {
    NONE("None", 0),
    SMALL("Small", 1),
    MEDIUM("Medium", 2),
    LARGE("Large", 4);

    private final String label;
    private final int multiplier;

    DamageLevel(String label, int multiplier) {
        this.label = label;
        this.multiplier = multiplier;
    }

    public int getMultiplier() { return multiplier; }

    public boolean isDamaged() { return this != NONE; }

    @Override
    public String toString() { return label; }
}
