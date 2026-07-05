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
        sb.append("========== RENTAL BILL ==========\n");
        sb.append("Rental ID   : ").append(rental.getRentalId()).append("\n");
        sb.append("Equipment   : ").append(rental.getEquipment().getName())
          .append(" (").append(rental.getEquipment().getCategory()).append(")\n");
        sb.append("Renter      : ").append(rental.getUser().getFullName())
          .append(" [").append(rental.getUser().getRole()).append("]\n");
        sb.append("Pricing Plan: ").append(strategyUsed).append("\n");
        sb.append("----------------------------------\n");
        sb.append(String.format("Base Rental Fee : RM %8.2f%n", baseFee));
        sb.append(String.format("Discount        : RM %8.2f%n", -discount));
        sb.append(String.format("Penalty         : RM %8.2f%n", penalty));
        sb.append("----------------------------------\n");
        sb.append(String.format("NET PAYABLE     : RM %8.2f%n", getNetPayable()));
        sb.append("==================================\n");
        return sb.toString();
    }
}
