package rental.model.billing;

import rental.model.rental.Rental;
import rental.model.equipment.Equipment;
import rental.model.user.User;
import rental.model.user.UserRole;
import rental.model.penalty.DamageSeverity;
import rental.model.penalty.DamagePenalty;
import rental.model.penalty.LateReturnPenalty;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BillCalculator {
    private static BillCalculator instance;
    private final DamagePenalty damagePenalty;
    private final LateReturnPenalty lateReturnPenalty;

    private BillCalculator() {
        this.damagePenalty = new DamagePenalty();
        this.lateReturnPenalty = new LateReturnPenalty();
    }

    public static BillCalculator getInstance() {
        if (instance == null) {
            instance = new BillCalculator();
        }
        return instance;
    }

    public Bill calculate(String billId, Rental rental, Equipment equipment,
                         User user, DamageSeverity severity) {
        int days = rental.getRentalDays();
        double subtotal = equipment.calculateRentalFee(days);
        double discountRate = user.getDiscountRate();
        double discount = discountRate * subtotal;

        LocalDate actualReturnDate = rental.getActualReturnDate();
        LocalDate dueDate = rental.getDueDate();

        int lateDays = 0;
        if (actualReturnDate != null && actualReturnDate.isAfter(dueDate)) {
            lateDays = (int) ChronoUnit.DAYS.between(dueDate, actualReturnDate);
        }
        double latePenalty = lateReturnPenalty.computeLatePenalty(equipment, lateDays);

        double damagePenaltyAmount = damagePenalty.computeDamagePenalty(severity);

        return new Bill(billId, rental, equipment, user,
                       equipment.getPlanName(),
                       subtotal, discount, latePenalty, damagePenaltyAmount);
    }
}
