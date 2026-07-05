package model;

/**
 * Abstract base class for a system user. Demonstrates INHERITANCE and
 * ABSTRACTION: each concrete user type defines its own discount eligibility,
 * which is later used to pick the appropriate PricingStrategy (Bridge).
 *
 * Mutable by ID: userId is fixed for the object's lifetime (it's the DB key
 * and what active Rentals reference), but display fields can be edited
 * in place so existing Rental/Bill objects immediately see the update
 * instead of pointing at a stale, replaced instance.
 */
public abstract class User {

    private final String userId;
    private String fullName;

    protected User(String userId, String fullName) {
        this.userId = userId;
        this.fullName = fullName;
    }

    /** Whether this user qualifies for special/discounted pricing. */
    public abstract boolean isEligibleForDiscount();

    /** Role label shown in the UI, e.g. "Student" or "Staff". */
    public abstract String getRole();

    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User other)) return false;
        return userId.equals(other.userId);
    }

    @Override
    public int hashCode() { return userId.hashCode(); }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", fullName, userId, getRole());
    }
}
