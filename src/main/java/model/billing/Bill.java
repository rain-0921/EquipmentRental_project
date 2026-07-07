package model.billing;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Detailed bill produced by the billing service for a returned
 * rental. Held 1:1 with {@code rental} via {@code bill.rental_id}.
 */
public class Bill {

    private final int billId;
    private final int rentalId;
    private final double baseRentalFee;
    private final double discountAmount;
    private final double penaltyAmount;
    private final double netPayable;
    private final LocalDateTime issuedAt;

    public Bill(int billId, int rentalId,
                double baseRentalFee, double discountAmount,
                double penaltyAmount, double netPayable,
                LocalDateTime issuedAt) {
        this.billId = billId;
        this.rentalId = rentalId;
        this.baseRentalFee = baseRentalFee;
        this.discountAmount = discountAmount;
        this.penaltyAmount = penaltyAmount;
        this.netPayable = netPayable;
        this.issuedAt = Objects.requireNonNullElseGet(issuedAt, LocalDateTime::now);
    }

    public int getBillId()              { return billId; }
    public int getRentalId()            { return rentalId; }
    public double getBaseRentalFee()    { return baseRentalFee; }
    public double getDiscountAmount()   { return discountAmount; }
    public double getPenaltyAmount()    { return penaltyAmount; }
    public double getNetPayable()       { return netPayable; }
    public LocalDateTime getIssuedAt()  { return issuedAt; }
}