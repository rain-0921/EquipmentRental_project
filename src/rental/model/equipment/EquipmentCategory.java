package rental.model.equipment;

public enum EquipmentCategory {
    ELECTRONICS(20.0),
    MEDIA(10.0),
    LABORATORY(30.0);

    private final double lateFeePerDay;

    EquipmentCategory(double lateFeePerDay) {
        this.lateFeePerDay = lateFeePerDay;
    }

    public double lateFeePerDay() {
        return lateFeePerDay;
    }

    public String idPrefix() {
        switch (this) {
            case ELECTRONICS: return "E";
            case MEDIA:       return "M";
            case LABORATORY:  return "L";
            default:          return "X";
        }
    }
}
