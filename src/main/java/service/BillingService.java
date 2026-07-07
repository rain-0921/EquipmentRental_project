package service;

import model.billing.Bill;
import model.equipment.EquipmentItem;
import model.equipment.EquipmentStatus;
import model.rental.Rental;
import model.rental.Rental.DamageLevel;
import model.user.User;
import db.DatabaseManager;
import repository.BillRepository;
import repository.EquipmentRepository;
import repository.RentalRepository;
import repository.UserRepository;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Coordinates the return-and-bill workflow. Wraps the whole thing
 * in a single JDBC transaction so a failure mid-way doesn't leave
 * the rental and bill tables out of sync.
 */
public class BillingService {

    private final UserRepository users;
    private final EquipmentRepository equipment;
    private final RentalRepository rentals;
    private final BillRepository bills;

    public BillingService() {
        this(new UserRepository(),
             new EquipmentRepository(),
             new RentalRepository(),
             new BillRepository());
    }

    /** Constructor for unit tests - inject mocks. */
    public BillingService(UserRepository users,
                          EquipmentRepository equipment,
                          RentalRepository rentals,
                          BillRepository bills) {
        this.users = users;
        this.equipment = equipment;
        this.rentals = rentals;
        this.bills = bills;
    }

    /**
     * Process the return of a rental: update the rental row, flip
     * the equipment back to AVAILABLE, then create a detailed bill
     * and persist it. Returns the freshly-created bill.
     */
    public Bill processReturn(int rentalId,
                              LocalDate returnDate,
                              DamageLevel damageLevel) throws Exception {
        Objects.requireNonNull(returnDate, "returnDate");
        Objects.requireNonNull(damageLevel, "damageLevel");

        // Load the rental first - outside the tx because we need to
        // resolve user + equipment for the calculation.
        Rental rental = rentals.findAll().stream()
            .filter(r -> r.getRentalId() == rentalId)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Unknown rental id: " + rentalId));

        User user = users.findById(rental.getUserId())
            .orElseThrow(() -> new IllegalStateException(
                "Orphan rental: user " + rental.getUserId() + " not found"));

        EquipmentItem item = equipment.findAll().stream()
            .filter(e -> e.getEquipmentId().equals(rental.getEquipmentId()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Orphan rental: equipment " + rental.getEquipmentId() + " not found"));

        Bill bill = calculateBill(user, item, rental, returnDate, damageLevel);

        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                rentals.markReturned(rentalId, returnDate, damageLevel, Rental.Status.RETURNED);
                equipment.setAvailability(item.getEquipmentId(), true);
                // Capture the id that the DB actually assigned so the
                // caller (and the receipt it builds) can reference it.
                Bill persisted = bills.insert(bill);
                c.commit();
                item.setStatus(EquipmentStatus.AVAILABLE);
                return persisted;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    /**
     * Pure calculation - exposed package-private so the GUI's bill
     * preview panel can call it without touching the DB.
     */
    public Bill calculateBill(User user,
                              EquipmentItem item,
                              Rental rental,
                              LocalDate returnDate,
                              DamageLevel damageLevel) {
        int days       = rental.rentalDays();
        int daysLate   = rental.daysLate(returnDate);

        // 1. Base fee (delegated to the bridged PricingPolicy).
        double baseFee = item.calculateBaseFee(days);

        // 2. Penalty (pricing strategy x category multiplier x damage fee).
        double pricingPenalty   = item.calculateLatePenalty(daysLate);
        double categoryPenalty  = baseFee * 0.10 * daysLate
                                * item.getCategoryLatePenaltyMultiplier();
        double damagePenalty    = damageLevel.getFee();
        double penaltyTotal     = round2(pricingPenalty + categoryPenalty + damagePenalty);

        // 3. Discount applied to (baseFee + penaltyTotal).
        double discount         = round2((baseFee + penaltyTotal) * user.getDiscountRate());

        // 4. Net payable.
        double net              = round2(baseFee + penaltyTotal - discount);

        Bill bill = new Bill(
            0,
            rental.getRentalId(),
            baseFee,
            discount,
            penaltyTotal,
            net,
            null);

        // Cache the breakdown on the rental for the receipt screen.
        rental.setReturnDate(returnDate);
        rental.setDamageLevel(damageLevel);
        return bill;
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}