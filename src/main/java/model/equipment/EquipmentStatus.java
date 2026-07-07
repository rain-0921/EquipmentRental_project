package model.equipment;

/**
 * Available status for an equipment item. Used by the GUI to disable
 * the "Rent" button and to flag overdue rentals.
 */
public enum EquipmentStatus {
    AVAILABLE("Available"),
    RENTED("Rented"),
    OVERDUE("Overdue"),
    UNDER_MAINTENANCE("Under Maintenance");

    private final String displayName;

    EquipmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}