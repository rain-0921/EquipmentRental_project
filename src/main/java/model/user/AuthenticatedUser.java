package model.user;

import java.time.LocalDateTime;

/**
 * A {@link User} enriched with a stored password hash. Returned by
 * {@code UserRepository.findByEmail} for the authentication flow.
 * Keeps the credential off the base {@link User} class so unrelated
 * code paths (billing, equipment) never accidentally receive it.
 */
public final class AuthenticatedUser extends User {

    private final String passwordHash;
    private final double discountRate;
    private final boolean isFinalYear;

    public AuthenticatedUser(String userId, String fullName, String email,
                             UserType type, double discountRate,
                             boolean isFinalYear,
                             LocalDateTime createdAt, String passwordHash) {
        super(userId, fullName, email, type, createdAt);
        this.passwordHash = passwordHash;
        this.discountRate = discountRate;
        this.isFinalYear  = isFinalYear;
    }

    /** Back-compat ctor. */
    public AuthenticatedUser(String userId, String fullName, String email,
                             UserType type, double discountRate,
                             LocalDateTime createdAt, String passwordHash) {
        this(userId, fullName, email, type, discountRate, false, createdAt, passwordHash);
    }

    public String passwordHash() { return passwordHash; }

    @Override
    public double getDiscountRate() {
        return discountRate;
    }

    @Override
    public boolean isFinalYear() {
        return isFinalYear;
    }
}