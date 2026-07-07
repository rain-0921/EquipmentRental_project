package model.user;

import java.time.LocalDateTime;

/**
 * Student discount policy (v2):
 *   * Non-final-year students get no discount.
 *   * Final-year students get 15 % off.
 * Per-row overrides from the DB take precedence - the values stored in
 * {@code user.discount_rate} and {@code user.is_final_year} are the
 * source of truth, this class just provides sensible defaults when no
 * row is supplied at construction time.
 */
public class Student extends User {

    private static final double NON_FINAL_YEAR_DISCOUNT = 0.00;
    private static final double FINAL_YEAR_DISCOUNT     = 0.15;

    private final double   discountRate;
    private final boolean  isFinalYear;

    public Student(String userId, String fullName, String email,
                   double discountRate, boolean isFinalYear,
                   LocalDateTime createdAt) {
        super(userId, fullName, email, UserType.STUDENT, createdAt);
        this.discountRate = validate(discountRate);
        this.isFinalYear  = isFinalYear;
    }

    public Student(String userId, String fullName, String email,
                   double discountRate, LocalDateTime createdAt) {
        this(userId, fullName, email, discountRate, false, createdAt);
    }

    /** Default no-arg constructor: non-final-year, 0% discount. */
    public Student(String userId, String fullName, String email,
                   LocalDateTime createdAt) {
        this(userId, fullName, email, NON_FINAL_YEAR_DISCOUNT, false, createdAt);
    }

    @Override
    public double getDiscountRate() {
        return discountRate;
    }

    /** True when the student is in their final academic year. */
    public boolean isFinalYear() {
        return isFinalYear;
    }

    private static double validate(double r) {
        if (r < 0.0 || r > 1.0) {
            throw new IllegalArgumentException("discount out of range: " + r);
        }
        return r;
    }
}
