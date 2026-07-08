# Campus Equipment Rental System - API Contract

## Core Interfaces and Classes

### Equipment Domain (model.equipment)

#### Equipment (abstract class)
```java
public abstract class Equipment {
    public String getEquipmentId();
    public String getName();
    public String getDescription();
    public EquipmentCategory getCategory();
    public EquipmentStatus getStatus();
    public void setStatus(EquipmentStatus status);
    public double getDailyRate();
    public PricingPolicy getPricingPolicy();
    public PenaltyRule getPenaltyRule();
    public double calculateRentalFee(int days);
    public String getPlanName();
}
```

#### EquipmentStatus (enum)
```java
public enum EquipmentStatus {
    AVAILABLE,
    RENTED,
    DAMAGED
}
```

#### EquipmentCategory (enum)
```java
public enum EquipmentCategory {
    ELECTRONICS(20.0),
    MEDIA(10.0),
    LABORATORY(30.0);

    public double lateFeePerDay();
}
```

### Pricing Domain (model.pricing)

#### PricingPolicy (interface)
```java
public interface PricingPolicy {
    double computeRate(Equipment equipment, int days);
    String planName();
}
```

#### Implementations
- `StandardPricing`: 0% discount, planName = "Standard (0% off)"
- `FinalYearStudentDiscountPricing`: 15% discount, planName = "FYP Discount (15% off total)"
- `StaffPricing`: 20% discount, planName = "Staff Discount (20% off final)"

### Penalty Domain (model.penalty)

#### DamageSeverity (enum)
```java
public enum DamageSeverity {
    NONE, LIGHT, MODERATE, HEAVY
}
```

#### PenaltyRule (interface)
```java
public interface PenaltyRule {
    double computeDamagePenalty(DamageSeverity severity);
}
```

#### DamagePenalty
- LIGHT → RM 10
- MODERATE → RM 100
- HEAVY → RM 1000
- NONE → RM 0

### Rental Domain (model.rental)

#### Rental (class)
```java
public class Rental {
    public String getRentalId();
    public User getUser();
    public Equipment getEquipment();
    public int getRentalDays();
    public LocalDate getRentalDate();
    public LocalDate getDueDate();
    public LocalDate getActualReturnDate();
    public void setActualReturnDate(LocalDate date);
    public RentalStatus getStatus();
    public void setStatus(RentalStatus status);
    public DamageSeverity getReportedSeverity();
    public void setReportedSeverity(DamageSeverity severity);
    public DamageSeverity getFinalSeverity();
    public void setFinalSeverity(DamageSeverity severity);
    public Bill getBill();
    public void setBill(Bill bill);
    public int getLateDays();
}
```

#### RentalStatus (enum)
```java
public enum RentalStatus {
    ACTIVE,
    RETURN_REQUESTED,
    APPROVED,
    REJECTED
}
```

### User Domain (model.user)

#### User (abstract class)
```java
public abstract class User {
    public String getUserId();
    public String getName();
    public String getPassword();
    public UserRole getRole();
    public abstract double getDiscountRate();
}
```

#### UserRole (enum)
```java
public enum UserRole {
    STUDENT,
    FINAL_YEAR_STUDENT,
    STAFF
}
```

### Billing Domain (model.billing)

#### Bill (class)
```java
public class Bill {
    public String getBillId();
    public String getRentalId();
    public String getEquipmentName();
    public String getRenterName();
    public String getPricingPlan();
    public double getSubtotal();
    public double getDiscount();
    public double getLatePenalty();
    public double getDamagePenalty();
    public double getNetPayable();
}
```

#### BillCalculator
```java
public class BillCalculator {
    public static BillCalculator getInstance();
    public Bill calculate(String billId, Rental rental, Equipment equipment,
                         User user, DamageSeverity severity);
}
```

**Calculation Formula:**
```
netPayable = subtotal - discount + latePenalty + damagePenalty
```

### Services

#### AuthService
```java
public class AuthService {
    public static AuthService getInstance();
    public User authenticate(String userId, String password);
    public User getCurrentUser();
    public void logout();
    public boolean isLoggedIn();
    public boolean isStaff();
}
```

#### RentalService
```java
public class RentalService {
    public static RentalService getInstance();
    public String rentEquipment(String equipmentId, User user, int days);
    public List<Equipment> getAvailableEquipment();
    public List<Equipment> getAllEquipment();
    public List<Equipment> searchEquipment(String keyword);
    public List<Rental> getUserRentals(User user);
    public List<Rental> getActiveRentalsForUser(User user);
    public String submitReturnRequest(String rentalId);
}
```

#### ApprovalService
```java
public class ApprovalService {
    public static ApprovalService getInstance();
    public List<Rental> getPendingApprovals();
    public String approveReturn(String rentalId, DamageSeverity severity);
    public String rejectReturn(String rentalId);
}
```

#### UserService
```java
public class UserService {
    public static UserService getInstance();
    public String createUser(String userId, String name, String password, UserRole role);
    public User getUser(String userId);
    public List<User> getAllUsers();
    public String updateUser(String userId, String name, String password);
    public boolean deleteUser(String userId);
}
```

#### EquipmentService
```java
public class EquipmentService {
    public static EquipmentService getInstance();
    public String createEquipment(String equipmentId, String name, String description,
                                  EquipmentCategory category, double dailyRate);
    public Equipment getEquipment(String equipmentId);
    public List<Equipment> getAllEquipment();
    public String updateEquipment(String equipmentId, String name, String description,
                                 EquipmentCategory category, double dailyRate);
    public String deleteEquipment(String equipmentId);
    public String markAsRepaired(String equipmentId);
    public String updateEquipmentStatus(String equipmentId, EquipmentStatus status);
}
```

### Repositories

All repositories are Singletons with `getInstance()` method.

- `EquipmentRepository`: CRUD for Equipment
- `UserRepository`: CRUD for User + authentication
- `RentalRepository`: CRUD for Rental + rental ID generation
- `BillRepository`: CRUD for Bill + bill ID generation
