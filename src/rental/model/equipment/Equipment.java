package rental.model.equipment;

import rental.model.pricing.PricingPolicy;
import rental.model.penalty.PenaltyRule;

public abstract class Equipment {
    protected String equipmentId;
    protected String name;
    protected String description;
    protected EquipmentCategory category;
    protected EquipmentStatus status;
    protected double dailyRate;
    protected PricingPolicy pricingPolicy;
    protected PenaltyRule penaltyRule;

    public Equipment(String equipmentId, String name, String description,
                    EquipmentCategory category, double dailyRate,
                    PricingPolicy pricingPolicy, PenaltyRule penaltyRule) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.dailyRate = dailyRate;
        this.pricingPolicy = pricingPolicy;
        this.penaltyRule = penaltyRule;
        this.status = EquipmentStatus.AVAILABLE;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public EquipmentCategory getCategory() {
        return category;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public double getDailyRate() {
        return dailyRate;
    }

    public PricingPolicy getPricingPolicy() {
        return pricingPolicy;
    }

    public PenaltyRule getPenaltyRule() {
        return penaltyRule;
    }

    public double calculateRentalFee(int days) {
        return pricingPolicy.computeRate(this, days);
    }

    public String getPlanName() {
        return pricingPolicy.planName();
    }
}
