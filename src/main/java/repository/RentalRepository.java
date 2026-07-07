package repository;

import model.rental.Rental;
import model.rental.Rental.DamageLevel;

import db.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD operations for the {@code rental} table. */
public class RentalRepository {

    private static final String SELECT_ALL =
        "SELECT rental_id, user_id, equipment_id, rent_date, due_date, "
      + "       return_date, damage_level, status "
      + "FROM rental ORDER BY rent_date DESC, rental_id DESC";

    private static final String SELECT_BY_USER =
        "SELECT rental_id, user_id, equipment_id, rent_date, due_date, "
      + "       return_date, damage_level, status "
      + "FROM rental WHERE user_id = ? ORDER BY rent_date DESC";

    private static final String SELECT_ACTIVE_FOR_EQUIPMENT =
        "SELECT rental_id, user_id, equipment_id, rent_date, due_date, "
      + "       return_date, damage_level, status "
      + "FROM rental WHERE equipment_id = ? AND status IN ('ACTIVE','OVERDUE')";

    private static final String INSERT =
        "INSERT INTO rental (user_id, equipment_id, rent_date, due_date, status, damage_level) "
      + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_RETURN =
        "UPDATE rental SET return_date = ?, damage_level = ?, status = ? "
      + "WHERE rental_id = ?";

    public List<Rental> findAll() throws SQLException {
        List<Rental> rentals = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rentals.add(mapRow(rs));
        }
        return rentals;
    }

    public List<Rental> findByUser(String userId) throws SQLException {
        List<Rental> rentals = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_USER)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) rentals.add(mapRow(rs));
            }
        }
        return rentals;
    }

    public Optional<Rental> findActiveRentalForEquipment(String equipmentId) throws SQLException {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ACTIVE_FOR_EQUIPMENT)) {
            ps.setString(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /** Inserts a new rental and returns it with the DB-generated id. */
    public Rental insert(Rental r) throws SQLException {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getUserId());
            ps.setString(2, r.getEquipmentId());
            ps.setDate(3, Date.valueOf(r.getRentDate()));
            ps.setDate(4, Date.valueOf(r.getDueDate()));
            ps.setString(5, r.getStatus().name());
            ps.setString(6, r.getDamageLevel().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Rental(
                        keys.getInt(1),
                        r.getUserId(),
                        r.getEquipmentId(),
                        r.getRentDate(),
                        r.getDueDate(),
                        r.getReturnDate(),
                        r.getDamageLevel(),
                        r.getStatus());
                }
            }
        }
        return r;
    }

    public void markReturned(int rentalId, java.time.LocalDate returnDate,
                             DamageLevel damageLevel, Rental.Status status) throws SQLException {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE_RETURN)) {
            ps.setDate(1, Date.valueOf(returnDate));
            ps.setString(2, damageLevel.name());
            ps.setString(3, status.name());
            ps.setInt(4, rentalId);
            ps.executeUpdate();
        }
    }

    private Rental mapRow(ResultSet rs) throws SQLException {
        return new Rental(
            rs.getInt("rental_id"),
            rs.getString("user_id"),
            rs.getString("equipment_id"),
            rs.getDate("rent_date").toLocalDate(),
            rs.getDate("due_date").toLocalDate(),
            rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null,
            DamageLevel.parse(rs.getString("damage_level")),
            Rental.Status.valueOf(rs.getString("status")));
    }
}