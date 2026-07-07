package model.equipment;

/**
 * The three equipment categories supported by the system. Each value
 * maps to a different {@code EquipmentItem} subclass through the
 * abstract factory used inside the repository layer.
 */
public enum EquipmentCategory {
    ELECTRONICS("Electronics"),
    MEDIA("Media Equipment"),
    LABORATORY("Laboratory Equipment");

    private final String displayName;

    EquipmentCategory(String displayName) {
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