package rental.model.user;

public class StaffUser extends User {
    public StaffUser(String userId, String name, String password) {
        super(userId, name, password, UserRole.STAFF);
    }

    @Override
    public double getDiscountRate() {
        return 0.20;
    }

    public boolean isStaff() {
        return true;
    }
}
