package rental.model.user;

public class UserFactory {
    private static UserFactory instance;

    private UserFactory() {}

    public static UserFactory getInstance() {
        if (instance == null) {
            instance = new UserFactory();
        }
        return instance;
    }

    public User createUser(String userId, String name, String password, UserRole role) {
        return new User(userId, name, password, role,
                discountRateFor(role), planNameFor(role));
    }

    public double discountRateFor(UserRole role) {
        return switch (role) {
            case STUDENT -> 0.0;
            case FINAL_YEAR_STUDENT -> 0.15;
            case STAFF -> 0.20;
        };
    }

    public String planNameFor(UserRole role) {
        return switch (role) {
            case STUDENT -> "Standard (0% off)";
            case FINAL_YEAR_STUDENT -> "FYP Discount (15% off total)";
            case STAFF -> "Staff Discount (20% off final)";
        };
    }
}
