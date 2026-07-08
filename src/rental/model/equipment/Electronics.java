package rental.model.equipment;

public class Electronics extends Equipment {
    public Electronics(String equipmentId, String name, String description, double dailyRate) {
        super(equipmentId, name, description, EquipmentCategory.ELECTRONICS, dailyRate);
    }
}
