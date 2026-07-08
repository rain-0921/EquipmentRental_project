package rental.model.user;

public abstract class User {
    protected String userId;
    protected String name;
    protected String password;
    protected UserRole role;
    protected UserStatus status;

    public User(String userId, String name, String password, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public abstract double getDiscountRate();

    public String getPlanName() {
        return "Standard (0% off)";
    }
}
