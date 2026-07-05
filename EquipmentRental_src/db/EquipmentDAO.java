package db;

import model.Electronics;
import model.Equipment;
import model.LabEquipment;
import model.MediaEquipment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAO {

    public void insert(Equipment e) {
        String sql = "INSERT INTO equipment (equipment_id, name, category, daily_rate, available) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (equipment_id) DO UPDATE SET " +
                "name = EXCLUDED.name, category = EXCLUDED.category, " +
                "daily_rate = EXCLUDED.daily_rate, available = EXCLUDED.available";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getEquipmentId());
            ps.setString(2, e.getName());
            ps.setString(3, e.getCategory());
            ps.setDouble(4, e.getDailyRentalRate());
            ps.setBoolean(5, e.isAvailable());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save equipment: " + ex.getMessage(), ex);
        }
    }

    public void updateAvailability(String equipmentId, boolean available) {
        String sql = "UPDATE equipment SET available = ? WHERE equipment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setString(2, equipmentId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update equipment availability: " + ex.getMessage(), ex);
        }
    }

    public void delete(String equipmentId) {
        String sql = "DELETE FROM equipment WHERE equipment_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, equipmentId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete equipment (it may still be referenced by rental history): " + ex.getMessage(), ex);
        }
    }

    public List<Equipment> findAll() {
        List<Equipment> result = new ArrayList<>();
        String sql = "SELECT equipment_id, name, category, daily_rate, available FROM equipment ORDER BY equipment_id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load equipment: " + ex.getMessage(), ex);
        }
        return result;
    }

    private Equipment mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("equipment_id");
        String name = rs.getString("name");
        String category = rs.getString("category");
        double rate = rs.getDouble("daily_rate");
        boolean available = rs.getBoolean("available");

        return switch (category) {
            case "Media Equipment" -> new MediaEquipment(id, name, rate, available);
            case "Laboratory Equipment" -> new LabEquipment(id, name, rate, available);
            default -> new Electronics(id, name, rate, available);
        };
    }
}
