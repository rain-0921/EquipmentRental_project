package model.user;

/** Distinguishes STUDENT from STAFF for UI labelling and reports. */
public enum UserType {
    STUDENT("Student"),
    STAFF("Staff");

    private final String displayName;

    UserType(String displayName) { this.displayName = displayName; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }
}