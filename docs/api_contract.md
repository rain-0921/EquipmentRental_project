# Campus Equipment Rental System - API Contract

## Core Interfaces and Classes

### Equipment Domain (model.equipment)

#### Equipment (concrete class)
```java
public class Equipment {
    public String getEquipmentId();
    public String getName();
    public String getDescription();
    public EquipmentCategory getCategory();
    public EquipmentStatus getStatus();
    public void setStatus(EquipmentStatus status);
    public double getDailyRate();
    public double calculateRentalFee(int days);
}
```

`Electronics`, `MediaEquipment`, and `LaboratoryEquipment` are thin concrete
subclasses that just call the parent constructor with the right
`EquipmentCategory`.

#### EquipmentStatus (enum)
```java
public enum EquipmentStatus {
    AVAILABLE,
    RENTED,
    DAMAGED,
    INACTIVE
}
```

#### EquipmentCategory (enum)
```java
public enum EquipmentCategory {
    ELECTRONICS(20.0),
    MEDIA(10.0),
    LABORATORY(30.0);

    public double lateFeePerDay();
    public String idPrefix();
}
```

### Penalty Domain (model.penalty)

#### DamageSeverity (enum)
```java
public enum DamageSeverity {
    NONE, LIGHT, MODERATE, HEAVY
}
```

Penalty amounts live inside `BillCalculator` (see Billing Domain).

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

#### User (concrete class)
```java
public class User {
    public String getUserId();
    public String getName();
    public String getPassword();
    public UserRole getRole();
    public UserStatus getStatus();
    public void setStatus(UserStatus status);
    public double getDiscountRate();
    public void setDiscountRate(double rate);
    public String getPlanName();
    public void setPlanName(String plan);
}
```

The previous `StudentUser` / `FinalYearStudentUser` / `StaffUser` hierarchy
has been collapsed into a single `User` class with a `discountRate` field.
The discount rate and plan name are set by the `UserFactory` (Singleton)
based on the user's `UserRole`.

#### UserFactory (Singleton)
```java
public class UserFactory {
    public static UserFactory getInstance();
    public User createUser(String userId, String name, String password, UserRole role);
    public double discountRateFor(UserRole role);
    public String planNameFor(UserRole role);
}
```

Discount rate by role:

| Role | Discount | Plan name |
|---|---|---|
| STUDENT | 0% | Standard (0% off) |
| FINAL_YEAR_STUDENT | 15% | FYP Discount (15% off total) |
| STAFF | 20% | Staff Discount (20% off final) |

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

#### BillCalculator (Singleton)
```java
public class BillCalculator {
    public static BillCalculator getInstance();
    public Bill calculate(String billId, Rental rental, Equipment equipment,
                         User user, DamageSeverity severity);
}
```

**Calculation Formula:**
```
subtotal      = equipment.dailyRate * rental.rentalDays
discount      = user.discountRate * subtotal
latePenalty   = max(0, days_late) * equipment.category.lateFeePerDay
damagePenalty = { NONE: 0, LIGHT: 10, MODERATE: 100, HEAVY: 1000 }
netPayable    = subtotal - discount + latePenalty + damagePenalty
```

Damage and late-fee logic that previously lived in `DamagePenalty` and
`LateReturnPenalty` is now encapsulated inside `BillCalculator`.

### Services

All services are **Singletons** with `getInstance()`. They depend on
repositories (also Singletons) and `UserFactory` (Singleton) only.

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
    public String submitReturnRequest(String rentalId, String severity);
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
    public String inactivateUser(String userId);
    public String activateUser(String userId);
}
```

#### EquipmentService
```java
public class EquipmentService {
    public static EquipmentService getInstance();
    public String generateNextId(EquipmentCategory category);
    public String createEquipment(String name, String description,
                                  EquipmentCategory category, double dailyRate);
    public Equipment getEquipment(String equipmentId);
    public List<Equipment> getAllEquipment();
    public List<Equipment> getAllEquipmentIncludingInactive();
    public String updateEquipment(String equipmentId, String name, String description,
                                  EquipmentCategory category, double dailyRate);
    public String deleteEquipment(String equipmentId);
    public String activateEquipment(String equipmentId);
    public String canEditEquipment(String equipmentId);
    public String markAsRepaired(String equipmentId);
    public String updateEquipmentStatus(String equipmentId, EquipmentStatus status);
}
```

### Repositories

All repositories are **Singletons** with `getInstance()`.

- `DatabaseManager`: holds the single `Connection` to MySQL.
- `UserRepository`: CRUD for `User` + authentication. Uses `UserFactory.getInstance()` to build `User` objects from rows.
- `EquipmentRepository`: CRUD for `Equipment`.
- `RentalRepository`: CRUD for `Rental` + rental ID generation. Uses `UserFactory.getInstance()` to build `User` objects.
- `BillRepository`: CRUD for `Bill` + bill ID generation. Uses `UserFactory.getInstance()` to build `User` objects.

## Design Pattern

This system applies **exactly one design pattern: Singleton**.

| Class | Why Singleton? |
|---|---|
| `DatabaseManager` | one connection per JVM, opened lazily |
| `UserRepository` / `EquipmentRepository` / `RentalRepository` / `BillRepository` | shared by every service and every Swing panel |
| `AuthService` / `EquipmentService` / `RentalService` / `ApprovalService` / `UserService` | hold shared in-memory state (current user, etc.) |
| `BillCalculator` | the whole app must compute bills the same way |
| `UserFactory` | canonical place for role-based discount / plan rules |

No other GoF pattern is used. The previous `PricingPolicy` / `PenaltyRule`
Strategy, the `User` Template Method, and the `*Service` / `BillCalculator`
Façade were removed so the system has exactly one chosen design pattern.
