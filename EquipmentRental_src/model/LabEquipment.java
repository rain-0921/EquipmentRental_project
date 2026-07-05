package model;

/** Concrete equipment category: microscopes, sensors, lab kits, etc. */
public class LabEquipment extends Equipment {

    public LabEquipment(String name, double dailyRentalRate) {
        super(name, dailyRentalRate);
    }

    public LabEquipment(String equipmentId, String name, double dailyRentalRate, boolean available) {
        super(equipmentId, name, dailyRentalRate, available);
    }

    @Override
    public String getCategory() {
        return "Laboratory Equipment";
    }

    @Override
    public double getLatePenaltyRatePerDay() {
        // Lab tools may be shared/scarce -> highest penalty to discourage lateness
        return 20.00;
    }
}
