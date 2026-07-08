package rental.model.user;

public class StaffUser extends User {
    public StaffUser(String userId, String name, String password) {
        super(userId, name, password, UserRole.STAFF);
    }

    @Override
    public double getDiscountRate() {
        return 0.20;
    }

    @Override
    public String getPlanName() {
        return "Staff Discount (20% off final)";
    }

    public boolean isStaff() {
        return true;
    }
}
