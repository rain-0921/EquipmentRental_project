package rental.model.equipment;

public class MediaEquipment extends Equipment {
    public MediaEquipment(String equipmentId, String name, String description, double dailyRate) {
        super(equipmentId, name, description, EquipmentCategory.MEDIA, dailyRate);
    }
}
