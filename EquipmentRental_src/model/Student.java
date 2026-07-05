package model;

/** Student user. Final-year students are eligible for discounted pricing. */
public class Student extends User {

    private boolean finalYear;

    public Student(String userId, String fullName, boolean finalYear) {
        super(userId, fullName);
        this.finalYear = finalYear;
    }

    public boolean isFinalYear() { return finalYear; }
    public void setFinalYear(boolean finalYear) { this.finalYear = finalYear; }

    @Override
    public boolean isEligibleForDiscount() {
        return finalYear;
    }

    @Override
    public String getRole() {
        return finalYear ? "Student (Final Year)" : "Student";
    }
}
