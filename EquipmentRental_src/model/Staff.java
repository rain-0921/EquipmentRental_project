package model;

/** Staff user. Always eligible for staff discount pricing. */
public class Staff extends User {

    public Staff(String userId, String fullName) {
        super(userId, fullName);
    }

    @Override
    public boolean isEligibleForDiscount() {
        return true;
    }

    @Override
    public String getRole() {
        return "Staff";
    }
}
