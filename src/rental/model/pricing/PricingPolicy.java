package rental.model.pricing;

import rental.model.equipment.Equipment;

public interface PricingPolicy {
    double computeRate(Equipment equipment, int days);
    String planName();
}
