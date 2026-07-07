package service;

import model.equipment.EquipmentItem;
import model.equipment.EquipmentStatus;
import model.rental.Rental;
import model.user.User;
import db.DatabaseManager;
import repository.EquipmentRepository;
import repository.RentalRepository;
import repository.UserRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Handles the "create a rental" flow: validates inputs, creates the
 * rental row, flips the equipment to RENTED. Done in one tx.
 */
public class RentalService {

    private final UserRepository users;
    private final EquipmentRepository equipment;
    private final RentalRepository rentals;

    public RentalService() {
        this(new UserRepository(),
             new EquipmentRepository(),
             new RentalRepository());
    }

    public RentalService(UserRepository users,
                         EquipmentRepository equipment,
                         RentalRepository rentals) {
        this.users = users;
        this.equipment = equipment;
        this.rentals = rentals;
    }

    /**
     * @throws IllegalStateException if the equipment is not available
     *         or the user is unknown
     */
    public Rental rentEquipment(String userId,
                                String equipmentId,
                                LocalDate rentDate,
                                LocalDate dueDate) throws SQLException {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(equipmentId, "equipmentId");
        Objects.requireNonNull(rentDate, "rentDate");
        Objects.requireNonNull(dueDate, "dueDate");

        if (dueDate.isBefore(rentDate)) {
            throw new IllegalArgumentException("Due date is before rent date.");
        }

        User user = users.findById(userId).orElseThrow(
            () -> new IllegalStateException("Unknown user: " + userId));

        EquipmentItem item = equipment.findAll().stream()
            .filter(e -> e.getEquipmentId().equals(equipmentId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Unknown equipment: " + equipmentId));

        if (!item.isAvailable()) {
            throw new IllegalStateException(
                "Equipment is currently rented: " + item);
        }

        Rental rental = new Rental(userId, equipmentId, rentDate, dueDate);
        try (Connection c = DatabaseManager.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                Rental persisted = rentals.insert(rental);
                equipment.setAvailability(equipmentId, false);
                c.commit();
                item.setStatus(EquipmentStatus.RENTED);
                return persisted;
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            }
        }
    }
}