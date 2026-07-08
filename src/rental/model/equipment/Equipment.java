package rental.model.equipment;

public class Equipment {
    private String equipmentId;
    private String name;
    private String description;
    private EquipmentCategory category;
    private EquipmentStatus status;
    private double dailyRate;

    public Equipment(String equipmentId, String name, String description,
                     EquipmentCategory category, double dailyRate) {
        this.equipmentId = equipmentId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.dailyRate = dailyRate;
        this.status = EquipmentStatus.AVAILABLE;
    }

    public String getEquipmentId() { return equipmentId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public EquipmentCategory getCategory() { return category; }
    public EquipmentStatus getStatus() { return status; }

    public void setStatus(EquipmentStatus status) { this.status = status; }

    public double getDailyRate() { return dailyRate; }

    public double calculateRentalFee(int days) {
        return dailyRate * days;
    }
}
