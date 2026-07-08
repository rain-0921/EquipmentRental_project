package rental.model.equipment;

public class LaboratoryEquipment extends Equipment {
    public LaboratoryEquipment(String equipmentId, String name, String description, double dailyRate) {
        super(equipmentId, name, description, EquipmentCategory.LABORATORY, dailyRate);
    }
}
