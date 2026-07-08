package rental.model.billing;

import rental.model.rental.Rental;
import rental.model.equipment.Equipment;
import rental.model.user.User;
import rental.model.penalty.DamageSeverity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BillCalculator {
    private static BillCalculator instance;

    private BillCalculator() {}

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
        double latePenalty = computeLatePenalty(equipment.getCategory().lateFeePerDay(), lateDays);
        double damagePenaltyAmount = computeDamagePenalty(severity);

        return new Bill(billId, rental, equipment, user,
                        user.getPlanName(),
                        subtotal, discount, latePenalty, damagePenaltyAmount);
    }

    private double computeLatePenalty(double lateFeePerDay, int lateDays) {
        if (lateDays <= 0) return 0.0;
        return lateFeePerDay * lateDays;
    }

    private double computeDamagePenalty(DamageSeverity severity) {
        return switch (severity) {
            case LIGHT -> 10.0;
            case MODERATE -> 100.0;
            case HEAVY -> 1000.0;
            case NONE -> 0.0;
        };
    }
}
