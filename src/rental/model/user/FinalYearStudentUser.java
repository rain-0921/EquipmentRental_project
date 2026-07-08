package rental.model.user;

public class FinalYearStudentUser extends StudentUser {
    public FinalYearStudentUser(String userId, String name, String password) {
        super(userId, name, password);
        this.role = UserRole.FINAL_YEAR_STUDENT;
    }

    @Override
    public double getDiscountRate() {
        return 0.15;
    }

    @Override
    public String getPlanName() {
        return "FYP Discount (15% off total)";
    }
}
