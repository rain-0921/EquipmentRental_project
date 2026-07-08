package rental.model.user;

public class User {
    private String userId;
    private String name;
    private String password;
    private UserRole role;
    private UserStatus status;
    private double discountRate;
    private String planName;

    public User(String userId, String name, String password, UserRole role) {
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.role = role;
        this.status = UserStatus.ACTIVE;
    }

    public User(String userId, String name, String password, UserRole role,
                double discountRate, String planName) {
        this(userId, name, password, role);
        this.discountRate = discountRate;
        this.planName = planName;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }

    public void setStatus(UserStatus status) { this.status = status; }

    public double getDiscountRate() { return discountRate; }
    public void setDiscountRate(double discountRate) { this.discountRate = discountRate; }

    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
}
