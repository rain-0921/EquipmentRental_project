package model;

/** Concrete equipment category: cameras, projectors, microphones, etc. */
public class MediaEquipment extends Equipment {

    public MediaEquipment(String name, double dailyRentalRate) {
        super(name, dailyRentalRate);
    }

    public MediaEquipment(String equipmentId, String name, double dailyRentalRate, boolean available) {
        super(equipmentId, name, dailyRentalRate, available);
    }

    @Override
    public String getCategory() {
        return "Media Equipment";
    }

    @Override
    public double getLatePenaltyRatePerDay() {
        return 10.00;
    }
}
