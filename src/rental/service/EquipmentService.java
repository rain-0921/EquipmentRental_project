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

    public String createEquipment(String equipmentId, String name, String description,
                                  EquipmentCategory category, double dailyRate) {
        if (equipmentRepository.getEquipment(equipmentId) != null) {
            return "Equipment ID already exists";
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

    public String updateEquipment(String equipmentId, String name, String description, 
                                   EquipmentCategory category, double dailyRate) {
        Equipment oldEquipment = equipmentRepository.getEquipment(equipmentId);
        if (oldEquipment == null) {
            return "Equipment not found";
        }
        
        equipmentRepository.deleteEquipment(equipmentId);
        
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
        equipmentRepository.addEquipment(newEquipment);
        return "SUCCESS";
    }

    public String deleteEquipment(String equipmentId) {
        Equipment equipment = equipmentRepository.getEquipment(equipmentId);
        if (equipment == null) {
            return "Equipment not found";
        }
        
        if (rentalRepository.hasActiveRentalForEquipment(equipmentId)) {
            return "Cannot delete equipment with active rentals";
        }
        
        equipmentRepository.deleteEquipment(equipmentId);
        return "SUCCESS";
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
