package model.user;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Abstract base for anyone who can rent equipment. Concrete
 * subclasses decide what discount rate applies. Kept intentionally
 * minimal so the User side stays decoupled from the pricing /
 * equipment side - the Bridge lives between EquipmentItem and
 * PricingPolicy, not here.
 */
public abstract class User {

    private final String userId;
    private final String fullName;
    private final String email;
    private final UserType type;
    private final LocalDateTime createdAt;

    protected User(String userId, String fullName, String email,
                   UserType type, LocalDateTime createdAt) {
        this.userId   = Objects.requireNonNull(userId, "userId");
        this.fullName = Objects.requireNonNull(fullName, "fullName");
        this.email    = Objects.requireNonNull(email, "email");
        this.type     = Objects.requireNonNull(type, "type");
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /** Discount applied AFTER category-specific penalties but
     *  BEFORE final rounding. Range 0.0 - 1.0. */
    public abstract double getDiscountRate();

    /** True for students in their final year. Always false for staff. */
    public boolean isFinalYear() {
        return false;
    }

    public String getUserId()     { return userId; }
    public String getFullName()   { return fullName; }
    public String getEmail()      { return email; }
    public UserType getType()     { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return String.format("%s (%s, %s%s) - %.0f%% discount",
            fullName, userId, type,
            isFinalYear() ? ", final year" : "",
            getDiscountRate() * 100);
    }
}