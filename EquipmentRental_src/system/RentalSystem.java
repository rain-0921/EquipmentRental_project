package system;

import db.BillDAO;
import db.DatabaseManager;
import db.EquipmentDAO;
import db.IdSequenceDAO;
import db.RentalDAO;
import db.UserDAO;
import model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central manager that aggregates Equipment, Users, and Rentals, and keeps
 * them synchronized with the PostgreSQL database via the DAO classes in
 * the `db` package. Equipment/User/Rental/Bill objects are cached in memory
 * for the GUI, but every mutation is written through to the database.
 */
public class RentalSystem {

    private final List<Equipment> equipmentList = new ArrayList<>();
    private final List<User> userList = new ArrayList<>();
    private final List<Rental> rentalList = new ArrayList<>();

    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final UserDAO userDAO = new UserDAO();
    private final RentalDAO rentalDAO = new RentalDAO();
    private final BillDAO billDAO = new BillDAO();
    private final IdSequenceDAO idSequenceDAO = new IdSequenceDAO();

    public RentalSystem() {
        DatabaseManager.initSchema();
        loadFromDatabase();
        if (equipmentList.isEmpty() && userList.isEmpty()) {
            seedSampleData();
        }
    }

    private void loadFromDatabase() {
        equipmentList.addAll(equipmentDAO.findAll());
        userList.addAll(userDAO.findAll());

        Map<String, Equipment> equipmentById = new HashMap<>();
        for (Equipment e : equipmentList) equipmentById.put(e.getEquipmentId(), e);
        Map<String, User> userById = new HashMap<>();
        for (User u : userList) userById.put(u.getUserId(), u);

        List<Rental> loaded = rentalDAO.findAll(equipmentById, userById);
        rentalList.addAll(loaded);
        for (Rental r : rentalList) {
            if (r.getReturnDate() != null) {
                billDAO.findByRentalAndAttach(r);
            }
        }
    }

    private void seedSampleData() {
        addEquipment(new Electronics("Dell Latitude Laptop", 25.00));
        addEquipment(new Electronics("iPad Pro 12.9\"", 20.00));
        addEquipment(new MediaEquipment("Canon EOS 90D Camera", 30.00));
        addEquipment(new MediaEquipment("Epson Projector", 18.00));
        addEquipment(new LabEquipment("Digital Microscope", 15.00));
        addEquipment(new LabEquipment("Oscilloscope", 22.00));

        addUser(new Student("S1001", "Ahmad Bin Ali", false));
        addUser(new Student("S1002", "Wei Ling Tan", true));
        addUser(new Staff("T2001", "Dr. Kumar"));
    }

    // ---- Equipment management ----
    public void addEquipment(Equipment e) {
        equipmentList.add(e);
        equipmentDAO.insert(e);
    }

    public List<Equipment> getAllEquipment() { return equipmentList; }
    public List<Equipment> getAvailableEquipment() {
        return equipmentList.stream().filter(Equipment::isAvailable).toList();
    }

    /** Persists edits made to an existing Equipment object's fields (name/rate/etc.). */
    public void updateEquipment(Equipment e) {
        equipmentDAO.insert(e); // insert() is an upsert (ON CONFLICT DO UPDATE)
    }

    /** Removes equipment from the system and the database. Throws if it is
     *  currently rented out or referenced by rental history in the database. */
    public void deleteEquipment(Equipment e) {
        if (!e.isAvailable()) {
            throw new IllegalStateException("Cannot delete \"" + e.getName() + "\" - it is currently rented out.");
        }
        equipmentDAO.delete(e.getEquipmentId());
        equipmentList.remove(e);
    }

    // ---- User management ----

    /**
     * Generates the next available user ID for the given role. IDs are never
     * recycled: once S1001 is handed out, it stays retired even if that user
     * is later deleted - the next Student ID will be S1002, not S1001 again.
     * This keeps every ID unique for the lifetime of the system, so rental
     * history and bills can never become ambiguous about who they belonged to.
     */
    public String generateNextUserId(String role) {
        String prefix = "Staff".equals(role) ? "T" : "S";

        // Seed the very first call above any legacy/seeded IDs that already
        // exist for this prefix (only matters before the sequence has a row).
        int startIfMissing = 1001;
        for (User u : userList) {
            if (u.getUserId().startsWith(prefix)) {
                try {
                    int n = Integer.parseInt(u.getUserId().substring(prefix.length()));
                    if (n + 1 > startIfMissing) startIfMissing = n + 1;
                } catch (NumberFormatException ignored) { }
            }
        }

        int next = idSequenceDAO.getAndAdvance(prefix, startIfMissing);
        return prefix + next;
    }

    public void addUser(User u) {
        boolean duplicate = userList.stream().anyMatch(existing -> existing.getUserId().equals(u.getUserId()));
        if (duplicate) {
            throw new IllegalStateException("A user with ID \"" + u.getUserId() + "\" already exists.");
        }
        userList.add(u);
        userDAO.insert(u);
    }
    public List<User> getAllUsers() { return userList; }

    /** Persists edits made to an existing User object's fields. */
    public void updateUser(User u) {
        userDAO.insert(u); // insert() is an upsert (ON CONFLICT DO UPDATE)
    }

    /** Removes a user from the system and the database. Throws if the user
     *  has any rental history (referenced by the rental table). */
    public void deleteUser(User u) {
        boolean hasRentals = rentalList.stream().anyMatch(r -> r.getUser().equals(u));
        if (hasRentals) {
            throw new IllegalStateException("Cannot delete \"" + u.getFullName() + "\" - they have rental history.");
        }
        userDAO.delete(u.getUserId());
        userList.remove(u);
    }

    // ---- Rental management ----
    public Rental createRental(Equipment equipment, User user, int plannedDays) {
        if (!equipment.isAvailable()) {
            throw new IllegalStateException("Equipment is not available for rental: " + equipment.getName());
        }
        Rental rental = new Rental(equipment, user, LocalDate.now(), plannedDays);
        rentalList.add(rental);

        // Persist: equipment availability flag + new rental row
        equipmentDAO.updateAvailability(equipment.getEquipmentId(), false);
        rentalDAO.insert(rental);
        return rental;
    }

    public Bill returnRental(Rental rental, LocalDate returnDate, DamageLevel damageLevel) {
        Bill bill = rental.returnEquipment(returnDate, damageLevel);

        // Persist: equipment availability, rental return info, and the generated bill
        equipmentDAO.updateAvailability(rental.getEquipment().getEquipmentId(), true);
        rentalDAO.markReturned(rental);
        billDAO.insert(bill);
        return bill;
    }

    public List<Rental> getAllRentals() { return rentalList; }

    public List<Rental> getActiveRentals() {
        return rentalList.stream().filter(r -> r.getReturnDate() == null).toList();
    }

    public Optional<Rental> findActiveRentalByEquipment(Equipment equipment) {
        return rentalList.stream()
                .filter(r -> r.getReturnDate() == null && r.getEquipment().equals(equipment))
                .findFirst();
    }
}
