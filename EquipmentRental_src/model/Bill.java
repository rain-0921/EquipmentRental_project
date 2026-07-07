package model;

/** Immutable billing breakdown produced when a Rental is returned. */
public class Bill {

    private final Rental rental;
    private final double baseFee;
    private final double discount;
    private final double penalty;
    private final String strategyUsed;

    public Bill(Rental rental, double baseFee, double discount, double penalty, String strategyUsed) {
        this.rental = rental;
        this.baseFee = baseFee;
        this.discount = discount;
        this.penalty = penalty;
        this.strategyUsed = strategyUsed;
    }

    public double getNetPayable() {
        return baseFee - discount + penalty;
    }

    public double getBaseFee() { return baseFee; }
    public double getDiscount() { return discount; }
    public double getPenalty() { return penalty; }
    public String getStrategyUsed() { return strategyUsed; }
    public Rental getRental() { return rental; }

    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        Equipment eq = rental.getEquipment();
        int lateDays = (int) java.time.temporal.ChronoUnit.DAYS.between(rental.getDueDate(), rental.getReturnDate());
        if (lateDays < 0) lateDays = 0;

        double dailyRate = eq.getDailyRentalRate();
        double lateRatePerDay = eq.getLatePenaltyRatePerDay();
        double latePenalty = lateDays * lateRatePerDay;
        double damagePenalty = (rental.getDamageLevel() == null)
                ? 0.0
                : dailyRate * rental.getDamageLevel().getMultiplier();

        sb.append("========== RENTAL BILL ==========\n");
        sb.append("Rental ID   : ").append(rental.getRentalId()).append("\n");
        sb.append("Equipment   : ").append(eq.getName())
          .append(" (").append(eq.getCategory()).append(")\n");
        sb.append("Renter      : ").append(rental.getUser().getFullName())
          .append(" [").append(rental.getUser().getRole()).append("]\n");
        sb.append("Pricing Plan: ").append(strategyUsed).append("\n");
        sb.append("----------------------------------\n");
        sb.append(String.format("Base Rental Fee : RM %8.2f%n", baseFee));
        sb.append(String.format("Discount        : RM %8.2f%n", -discount));
        if (lateDays > 0) {
            sb.append(String.format("  Late Surcharge: RM %8.2f  (%d day(s) late x RM %.2f/day for %s)%n",
                    latePenalty, lateDays, lateRatePerDay, eq.getCategory()));
        }
        if (damagePenalty > 0) {
            sb.append(String.format("  Damage Charge : RM %8.2f  (%s x%d of RM %.2f)%n",
                    damagePenalty, rental.getDamageLevel(), rental.getDamageLevel().getMultiplier(), dailyRate));
        }
        sb.append(String.format("Penalty         : RM %8.2f%n", penalty));
        sb.append("----------------------------------\n");
        sb.append(String.format("NET PAYABLE     : RM %8.2f%n", getNetPayable()));
        sb.append("==================================\n");
        return sb.toString();
    }
}
