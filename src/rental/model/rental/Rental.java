package rental.model.rental;

import rental.model.equipment.Equipment;
import rental.model.user.User;
import rental.model.penalty.DamageSeverity;
import rental.model.billing.Bill;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Rental {
    private String rentalId;
    private User user;
    private Equipment equipment;
    private int rentalDays;
    private LocalDate rentalDate;
    private LocalDate dueDate;
    private LocalDate actualReturnDate;
    private RentalStatus status;
    private DamageSeverity reportedSeverity;
    private DamageSeverity finalSeverity;
    private Bill bill;

    // Default constructor for database mapping
    public Rental() {
    }

    public Rental(String rentalId, User user, Equipment equipment, int rentalDays) {
        this.rentalId = rentalId;
        this.user = user;
        this.equipment = equipment;
        this.rentalDays = rentalDays;
        this.rentalDate = LocalDate.now();
        this.dueDate = rentalDate.plusDays(rentalDays);
        this.status = RentalStatus.ACTIVE;
    }

    public String getRentalId() {
        return rentalId;
    }

    public void setRentalId(String rentalId) {
        this.rentalId = rentalId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public void setEquipment(Equipment equipment) {
        this.equipment = equipment;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public void setRentalDays(int rentalDays) {
        this.rentalDays = rentalDays;
    }

    public LocalDate getRentalDate() {
        return rentalDate;
    }

    public void setRentalDate(LocalDate rentalDate) {
        this.rentalDate = rentalDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public RentalStatus getStatus() {
        return status;
    }

    public void setStatus(RentalStatus status) {
        this.status = status;
    }

    public DamageSeverity getReportedSeverity() {
        return reportedSeverity;
    }

    public void setReportedSeverity(DamageSeverity reportedSeverity) {
        this.reportedSeverity = reportedSeverity;
    }

    public DamageSeverity getFinalSeverity() {
        return finalSeverity;
    }

    public void setFinalSeverity(DamageSeverity finalSeverity) {
        this.finalSeverity = finalSeverity;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public int getLateDays() {
        LocalDate checkDate = actualReturnDate != null ? actualReturnDate : LocalDate.now();
        if (checkDate.isAfter(dueDate)) {
            return (int) ChronoUnit.DAYS.between(dueDate, checkDate);
        }
        return 0;
    }
}
