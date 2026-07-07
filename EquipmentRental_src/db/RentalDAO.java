package db;

import model.DamageLevel;
import model.Equipment;
import model.Rental;
import model.User;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RentalDAO {

    public void insert(Rental r) {
        String sql = "INSERT INTO rental (rental_id, equipment_id, user_id, rent_date, due_date, " +
                "return_date, damage_level, pricing_strategy) VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (rental_id) DO UPDATE SET " +
                "due_date = EXCLUDED.due_date, return_date = EXCLUDED.return_date, " +
                "damage_level = EXCLUDED.damage_level, pricing_strategy = EXCLUDED.pricing_strategy";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, r.getRentalId());
            ps.setString(2, r.getEquipment().getEquipmentId());
            ps.setString(3, r.getUser().getUserId());
            ps.setDate(4, Date.valueOf(r.getRentDate()));
            ps.setDate(5, Date.valueOf(r.getDueDate()));
            ps.setDate(6, r.getReturnDate() != null ? Date.valueOf(r.getReturnDate()) : null);
            ps.setString(7, r.getDamageLevel().name());
            ps.setString(8, r.getEquipment().getPricingStrategy().getStrategyName());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save rental: " + ex.getMessage(), ex);
        }
    }

    public void markReturned(Rental r) {
        String sql = "UPDATE rental SET return_date = ?, damage_level = ? WHERE rental_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(r.getReturnDate()));
            ps.setString(2, r.getDamageLevel().name());
            ps.setString(3, r.getRentalId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update rental return: " + ex.getMessage(), ex);
        }
    }

    /** Loads all rentals, resolving Equipment/User references from the provided lookup maps. */
    public List<Rental> findAll(Map<String, Equipment> equipmentById, Map<String, User> userById) {
        List<Rental> result = new ArrayList<>();
        String sql = "SELECT rental_id, equipment_id, user_id, rent_date, due_date, return_date, damage_level, pricing_strategy " +
                "FROM rental ORDER BY rental_id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Equipment eq = equipmentById.get(rs.getString("equipment_id"));
                User user = userById.get(rs.getString("user_id"));
                if (eq == null || user == null) continue; // skip orphaned rows

                Date returnDateSql = rs.getDate("return_date");
                LocalDate returnDate = returnDateSql != null ? returnDateSql.toLocalDate() : null;

                DamageLevel damageLevel = DamageLevel.NONE;
                String dlStr = rs.getString("damage_level");
                if (dlStr != null) {
                    try {
                        damageLevel = DamageLevel.valueOf(dlStr);
                    } catch (IllegalArgumentException ignored) { }
                }

                String strategyName = rs.getString("pricing_strategy");
                Rental rental = Rental.fromPersistedRow(
                        rs.getString("rental_id"), eq, user,
                        rs.getDate("rent_date").toLocalDate(),
                        rs.getDate("due_date").toLocalDate(),
                        returnDate,
                        damageLevel,
                        strategyName
                );
                result.add(rental);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load rentals: " + ex.getMessage(), ex);
        }
        return result;
    }
}
