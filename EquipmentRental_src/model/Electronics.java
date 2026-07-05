package model;

/** Concrete equipment category: laptops, tablets, etc. */
public class Electronics extends Equipment {

    public Electronics(String name, double dailyRentalRate) {
        super(name, dailyRentalRate);
    }

    public Electronics(String equipmentId, String name, double dailyRentalRate, boolean available) {
        super(equipmentId, name, dailyRentalRate, available);
    }

    @Override
    public String getCategory() {
        return "Electronics";
    }

    @Override
    public double getLatePenaltyRatePerDay() {
        // Electronics are high-value -> steeper late penalty
        return 15.00;
    }
}
