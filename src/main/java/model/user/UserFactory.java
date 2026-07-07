package model.user;

import java.time.LocalDateTime;

/**
 * Builds the correct {@link User} subclass from a {@link UserType}
 * value. Mirrors {@code EquipmentItemFactory} - keeps the rest of
 * the codebase unaware of which concrete subclass to instantiate.
 */
public final class UserFactory {

    private UserFactory() { /* static helper */ }

    public static User create(String userId,
                              String fullName,
                              String email,
                              UserType type,
                              double discountRate,
                              boolean isFinalYear,
                              LocalDateTime createdAt) {
        return switch (type) {
            case STUDENT -> new Student(userId, fullName, email, discountRate, isFinalYear, createdAt);
            case STAFF   -> new Staff(userId, fullName, email, discountRate, createdAt);
        };
    }

    /** Back-compat overload for callers that don't track final-year status. */
    public static User create(String userId,
                              String fullName,
                              String email,
                              UserType type,
                              double discountRate,
                              LocalDateTime createdAt) {
        return create(userId, fullName, email, type, discountRate, false, createdAt);
    }

    /**
     * Build a {@link User} that also carries its stored password hash.
     * Used by the authentication flow.
     */
    public static AuthenticatedUser createWithCredentials(String userId,
                                                          String fullName,
                                                          String email,
                                                          UserType type,
                                                          double discountRate,
                                                          boolean isFinalYear,
                                                          LocalDateTime createdAt,
                                                          String passwordHash) {
        return new AuthenticatedUser(userId, fullName, email, type,
                                     discountRate, isFinalYear, createdAt, passwordHash);
    }

    /** Back-compat overload for callers that don't track final-year status. */
    public static AuthenticatedUser createWithCredentials(String userId,
                                                          String fullName,
                                                          String email,
                                                          UserType type,
                                                          double discountRate,
                                                          LocalDateTime createdAt,
                                                          String passwordHash) {
        return createWithCredentials(userId, fullName, email, type,
                                     discountRate, false, createdAt, passwordHash);
    }
}
