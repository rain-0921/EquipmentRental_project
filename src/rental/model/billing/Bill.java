package rental.model.billing;

import rental.model.rental.Rental;
import rental.model.equipment.Equipment;
import rental.model.user.User;

public class Bill {
    private String billId;
    private Rental rental;
    private String rentalId;
    private String equipmentName;
    private String renterName;
    private String pricingPlan;
    private double subtotal;
    private double discount;
    private double latePenalty;
    private double damagePenalty;
    private double netPayable;

    // Default constructor for database mapping
    public Bill() {
    }

    public Bill(String billId, Rental rental, Equipment equipment, User user,
               String pricingPlan, double subtotal, double discount,
               double latePenalty, double damagePenalty) {
        this.billId = billId;
        this.rental = rental;
        this.rentalId = rental.getRentalId();
        this.equipmentName = equipment.getName();
        this.renterName = user.getName();
        this.pricingPlan = pricingPlan;
        this.subtotal = subtotal;
        this.discount = discount;
        this.latePenalty = latePenalty;
        this.damagePenalty = damagePenalty;
        this.netPayable = subtotal - discount + latePenalty + damagePenalty;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public Rental getRental() {
        return rental;
    }

    public void setRental(Rental rental) {
        this.rental = rental;
    }

    public String getRentalId() {
        return rentalId;
    }

    public void setRentalId(String rentalId) {
        this.rentalId = rentalId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getRenterName() {
        return renterName;
    }

    public void setRenterName(String renterName) {
        this.renterName = renterName;
    }

    public String getPricingPlan() {
        return pricingPlan;
    }

    public void setPricingPlan(String pricingPlan) {
        this.pricingPlan = pricingPlan;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getLatePenalty() {
        return latePenalty;
    }

    public void setLatePenalty(double latePenalty) {
        this.latePenalty = latePenalty;
    }

    public double getDamagePenalty() {
        return damagePenalty;
    }

    public void setDamagePenalty(double damagePenalty) {
        this.damagePenalty = damagePenalty;
    }

    public double getNetPayable() {
        return netPayable;
    }

    public void setNetPayable(double netPayable) {
        this.netPayable = netPayable;
    }
}
