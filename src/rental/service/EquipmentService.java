package rental.service;

import rental.repo.EquipmentRepository;
import rental.repo.RentalRepository;
import rental.model.equipment.Equipment;
import rental.model.equipment.EquipmentStatus;
import rental.model.equipment.EquipmentCategory;
import rental.model.equipment.Electronics;
import rental.model.equipment.MediaEquipment;
import rental.model.equipment.LaboratoryEquipment;
import rental.model.pricing.PricingPolicy;
import rental.model.pricing.StandardPricing;
import rental.model.penalty.PenaltyRule;
import rental.model.penalty.DamagePenalty;

import java.util.List;

public class EquipmentService {
    private static EquipmentService instance;
    private final EquipmentRepository equipmentRepository;
    private final RentalRepository rentalRepository;

    private EquipmentService() {
        this.equipmentRepository = EquipmentRepository.getInstance();
        this.rentalRepository = RentalRepository.getInstance();
    }

    public static EquipmentService getInstance() {
        if (instance == null) {
            instance = new EquipmentService();
        }
        return instance;
    }

    public String generateNextId(EquipmentCategory category) {
        String prefix = category.idPrefix();
        int max = equipmentRepository.getMaxNumericSuffixForPrefix(prefix);
        return prefix + String.format("%03d", max + 1);
    }

    public String createEquipment(String name, String description,
                                  EquipmentCategory category, double dailyRate) {
        String equipmentId = generateNextId(category);

        if (equipmentRepository.getEquipment(equipmentId) != null) {
            String retryId = generateNextId(category);
            if (!retryId.equals(equipmentId)) {
                equipmentId = retryId;
            }
        }

        PricingPolicy pricing = new StandardPricing();
        DamagePenalty penalty = new DamagePenalty();

        Equipment equipment;
        switch (category) {
            case ELECTRONICS:
                equipment = new Electronics(equipmentId, name, description, dailyRate, pricing, penalty);
                break;
            case MEDIA:
                equipment = new MediaEquipment(equipmentId, name, description, dailyRate, pricing, penalty);
                break;
            case LABORATORY:
                equipment = new LaboratoryEquipment(equipmentId, name, description, dailyRate, pricing, penalty);
                break;
            default:
                return "Invalid category";
        }

        equipmentRepository.addEquipment(equipment);
        return "SUCCESS";
    }

    public Equipment getEquipment(String equipmentId) {
        return equipmentRepository.getEquipment(equipmentId);
    }

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.getAllEquipment();
    }

    public List<Equipment> getAllEquipmentIncludingInactive() {
        return equipmentRepository.getAllEquipmentIncludingInactive();
    }

    public String updateEquipment(String equipmentId, String name, String description,
                                   EquipmentCategory category, double dailyRate) {
        String editCheck = canEditEquipment(equipmentId);
        if (!editCheck.equals("CAN_EDIT")) {
            return editCheck;
        }

        Equipment oldEquipment = equipmentRepository.getEquipment(equipmentId);

        PricingPolicy pricing = oldEquipment.getPricingPolicy();
        PenaltyRule penalty = oldEquipment.getPenaltyRule();

        Equipment newEquipment;
        switch (category) {
            case ELECTRONICS:
                newEquipment = new Electronics(equipmentId, name, description, dailyRate, pricing, penalty);
                break;
            case MEDIA:
                newEquipment = new MediaEquipment(equipmentId, name, description, dailyRate, pricing, penalty);
                break;
            case LABORATORY:
                newEquipment = new LaboratoryEquipment(equipmentId, name, description, dailyRate, pricing, penalty);
                break;
            default:
                return "Invalid category";
        }

        newEquipment.setStatus(oldEquipment.getStatus());
        equipmentRepository.updateEquipment(newEquipment);
        return "SUCCESS";
    }

    public String deleteEquipment(String equipmentId) {
        Equipment equipment = equipmentRepository.getEquipment(equipmentId);
        if (equipment == null) {
            return "Equipment not found";
        }

        if (equipment.getStatus() == EquipmentStatus.INACTIVE) {
            return "Equipment is already inactive";
        }

        if (rentalRepository.hasActiveRentalForEquipment(equipmentId)) {
            return "Cannot inactivate equipment with active rentals";
        }

        equipmentRepository.updateStatus(equipmentId, EquipmentStatus.INACTIVE);
        return "SUCCESS";
    }

    public String activateEquipment(String equipmentId) {
        Equipment equipment = equipmentRepository.getEquipment(equipmentId);
        if (equipment == null) {
            return "Equipment not found";
        }

        if (equipment.getStatus() != EquipmentStatus.INACTIVE) {
            return "Equipment is already active";
        }

        equipmentRepository.updateStatus(equipmentId, EquipmentStatus.AVAILABLE);
        return "SUCCESS";
    }

    public String canEditEquipment(String equipmentId) {
        Equipment eq = getEquipment(equipmentId);
        if (eq == null) return "Equipment not found";

        if (eq.getStatus() == EquipmentStatus.INACTIVE) {
            return "Cannot edit inactive equipment";
        }
        if (eq.getStatus() == EquipmentStatus.RENTED) {
            return "Cannot edit equipment that is currently rented";
        }
        if (eq.getStatus() == EquipmentStatus.DAMAGED) {
            return "Cannot edit damaged equipment";
        }
        if (rentalRepository.hasPendingReturnRequest(equipmentId)) {
            return "Cannot edit equipment with pending return request";
        }
        return "CAN_EDIT";
    }

    public String markAsRepaired(String equipmentId) {
        Equipment equipment = equipmentRepository.getEquipment(equipmentId);
        if (equipment == null) {
            return "Equipment not found";
        }
        
        if (equipment.getStatus() != EquipmentStatus.DAMAGED) {
            return "Equipment is not in DAMAGED status";
        }
        
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipmentRepository.updateEquipment(equipment);
        return "SUCCESS";
    }

    public String updateEquipmentStatus(String equipmentId, EquipmentStatus status) {
        Equipment equipment = equipmentRepository.getEquipment(equipmentId);
        if (equipment == null) {
            return "Equipment not found";
        }
        
        equipment.setStatus(status);
        equipmentRepository.updateEquipment(equipment);
        return "SUCCESS";
    }
}
