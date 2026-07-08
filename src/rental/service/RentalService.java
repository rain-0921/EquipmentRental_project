package rental.service;

import rental.repo.EquipmentRepository;
import rental.repo.RentalRepository;
import rental.model.equipment.Equipment;
import rental.model.equipment.EquipmentStatus;
import rental.model.rental.Rental;
import rental.model.user.User;
import rental.model.rental.RentalStatus;
import rental.model.penalty.DamageSeverity;

import java.util.List;

public class RentalService {
    private static RentalService instance;
    private final EquipmentRepository equipmentRepository;
    private final RentalRepository rentalRepository;

    private RentalService() {
        this.equipmentRepository = EquipmentRepository.getInstance();
        this.rentalRepository = RentalRepository.getInstance();
    }

    public static RentalService getInstance() {
        if (instance == null) {
            instance = new RentalService();
        }
        return instance;
    }

    public String rentEquipment(String equipmentId, User user, int days) {
        Equipment equipment = equipmentRepository.getEquipment(equipmentId);
        
        if (equipment == null) {
            return "Equipment not found";
        }
        
        if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
            return "Equipment is not available for rental";
        }
        
        if (days <= 0) {
            return "Rental days must be a positive integer";
        }
        
        String rentalId = rentalRepository.generateRentalId();
        Rental rental = new Rental(rentalId, user, equipment, days);
        rentalRepository.addRental(rental);
        
        equipmentRepository.updateStatus(equipment.getEquipmentId(), EquipmentStatus.RENTED);
        
        return "SUCCESS:" + rentalId;
    }

    public List<Equipment> getAvailableEquipment() {
        return equipmentRepository.getAvailableEquipment();
    }

    public List<Equipment> getAllEquipment() {
        return equipmentRepository.getAllEquipment();
    }

    public List<Equipment> searchEquipment(String keyword) {
        return equipmentRepository.searchEquipment(keyword);
    }

    public List<Rental> getUserRentals(User user) {
        return rentalRepository.getRentalsByUser(user);
    }

    public List<Rental> getActiveRentalsForUser(User user) {
        return rentalRepository.getActiveRentalsByUser(user);
    }

    public String submitReturnRequest(String rentalId, String reportedSeverity) {
        Rental rental = rentalRepository.getRental(rentalId);
        
        if (rental == null) {
            return "Rental not found";
        }
        
        if (rental.getStatus() != RentalStatus.ACTIVE && 
            rental.getStatus() != RentalStatus.REJECTED) {
            return "Cannot submit return for this rental";
        }
        
        rental.setReportedSeverity(DamageSeverity.valueOf(reportedSeverity));
        rental.setStatus(RentalStatus.RETURN_REQUESTED);
        rentalRepository.updateRental(rental);
        
        return "SUCCESS";
    }
}
