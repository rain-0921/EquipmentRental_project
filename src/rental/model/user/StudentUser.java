package rental.model.user;

public class StudentUser extends User {
    public StudentUser(String userId, String name, String password) {
        super(userId, name, password, UserRole.STUDENT);
    }

    @Override
    public double getDiscountRate() {
        return 0.0;
    }
}
