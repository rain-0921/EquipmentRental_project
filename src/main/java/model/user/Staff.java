package model.user;

import java.time.LocalDateTime;

/** Staff discount policy (v2): flat 20 % off. The {@code discount_rate}
 *  column in the DB is the source of truth and can override this. */
public class Staff extends User {

    private static final double DEFAULT_DISCOUNT = 0.20;

    private final double discountRate;

    public Staff(String userId, String fullName, String email,
                 double discountRate, LocalDateTime createdAt) {
        super(userId, fullName, email, UserType.STAFF, createdAt);
        this.discountRate = validate(discountRate);
    }

    public Staff(String userId, String fullName, String email,
                 LocalDateTime createdAt) {
        this(userId, fullName, email, DEFAULT_DISCOUNT, createdAt);
    }

    @Override
    public double getDiscountRate() {
        return discountRate;
    }

    private static double validate(double r) {
        if (r < 0.0 || r > 1.0) {
            throw new IllegalArgumentException("discount out of range: " + r);
        }
        return r;
    }
}