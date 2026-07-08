package rental.model.penalty;

import rental.model.equipment.Equipment;

public class LateReturnPenalty {
    public double computeLatePenalty(Equipment equipment, int lateDays) {
        if (lateDays <= 0) {
            return 0.0;
        }
        return equipment.getCategory().lateFeePerDay() * lateDays;
    }
}
