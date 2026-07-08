package rental.service;

import rental.repo.RentalRepository;
import rental.repo.BillRepository;
import rental.repo.EquipmentRepository;
import rental.model.rental.Rental;
import rental.model.billing.Bill;
import rental.model.billing.BillCalculator;
import rental.model.equipment.EquipmentStatus;
import rental.model.penalty.DamageSeverity;
import rental.model.rental.RentalStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class ApprovalService {
    private static ApprovalService instance;
    private final RentalRepository rentalRepository;
    private final BillRepository billRepository;
    private final EquipmentRepository equipmentRepository;
    private final BillCalculator billCalculator;

    private ApprovalService() {
        this.rentalRepository = RentalRepository.getInstance();
        this.billRepository = BillRepository.getInstance();
        this.equipmentRepository = EquipmentRepository.getInstance();
        this.billCalculator = BillCalculator.getInstance();
    }

    public static ApprovalService getInstance() {
        if (instance == null) {
            instance = new ApprovalService();
        }
        return instance;
    }

    public List<Rental> getPendingApprovals() {
        List<Rental> pending = new ArrayList<>();
        for (Rental rental : rentalRepository.getAllRentals()) {
            if (rental.getStatus() == RentalStatus.RETURN_REQUESTED) {
                pending.add(rental);
            }
        }
        return pending;
    }

    public String approveReturn(String rentalId, DamageSeverity severity) {
        Rental rental = rentalRepository.getRental(rentalId);
        
        if (rental == null) {
            return "Rental not found";
        }
        
        if (rental.getStatus() != RentalStatus.RETURN_REQUESTED) {
            return "Rental is not pending approval";
        }
        
        rental.setActualReturnDate(LocalDate.now());
        rental.setFinalSeverity(severity);
        rental.setStatus(RentalStatus.APPROVED);
        
        String billId = billRepository.generateBillId();
        Bill bill = billCalculator.calculate(billId, rental, 
            rental.getEquipment(), rental.getUser(), severity);
        
        rental.setBill(bill);
        billRepository.addBill(bill);
        rentalRepository.updateRental(rental);
        
        if (severity != DamageSeverity.NONE) {
            equipmentRepository.updateStatus(
                rental.getEquipment().getEquipmentId(), 
                EquipmentStatus.DAMAGED
            );
        } else {
            equipmentRepository.updateStatus(
                rental.getEquipment().getEquipmentId(), 
                EquipmentStatus.AVAILABLE
            );
        }
        
        return "SUCCESS";
    }

    public String rejectReturn(String rentalId) {
        Rental rental = rentalRepository.getRental(rentalId);
        
        if (rental == null) {
            return "Rental not found";
        }
        
        if (rental.getStatus() != RentalStatus.RETURN_REQUESTED) {
            return "Rental is not pending approval";
        }
        
        rental.setStatus(RentalStatus.REJECTED);
        rentalRepository.updateRental(rental);
        
        return "SUCCESS";
    }
}
