package rental.repo;

import rental.model.equipment.*;
import rental.model.pricing.*;
import rental.model.penalty.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class EquipmentRepository {
    private static EquipmentRepository instance;
    private final DatabaseManager db;

    private EquipmentRepository() {
        this.db = DatabaseManager.getInstance();
    }

    public static EquipmentRepository getInstance() {
        if (instance == null) {
            instance = new EquipmentRepository();
        }
        return instance;
    }

    public void addEquipment(Equipment equipment) {
        String sql = "INSERT INTO equipment (equipment_id, name, description, category, status, daily_rate) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, equipment.getEquipmentId());
            pstmt.setString(2, equipment.getName());
            pstmt.setString(3, equipment.getDescription());
            pstmt.setString(4, equipment.getCategory().name());
            pstmt.setString(5, equipment.getStatus().name());
            pstmt.setDouble(6, equipment.getDailyRate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Equipment getEquipment(String equipmentId) {
        String sql = "SELECT * FROM equipment WHERE equipment_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, equipmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToEquipment(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Equipment> getAllEquipment() {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment WHERE status != 'INACTIVE'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                equipmentList.add(mapResultSetToEquipment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipmentList;
    }

    public List<Equipment> getAllEquipmentIncludingInactive() {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                equipmentList.add(mapResultSetToEquipment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipmentList;
    }

    public List<Equipment> getAvailableEquipment() {
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment WHERE status = 'AVAILABLE'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                equipmentList.add(mapResultSetToEquipment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipmentList;
    }

    public List<Equipment> searchEquipment(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        List<Equipment> equipmentList = new ArrayList<>();
        String sql = "SELECT * FROM equipment WHERE status != 'INACTIVE'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Equipment eq = mapResultSetToEquipment(rs);
                if (eq.getName().toLowerCase().contains(lowerKeyword) ||
                    eq.getEquipmentId().toLowerCase().contains(lowerKeyword) ||
                    eq.getCategory().name().toLowerCase().contains(lowerKeyword)) {
                    equipmentList.add(eq);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return equipmentList;
    }

    public void updateEquipment(Equipment equipment) {
        String sql = "UPDATE equipment SET name = ?, description = ?, category = ?, status = ?, daily_rate = ? WHERE equipment_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, equipment.getName());
            pstmt.setString(2, equipment.getDescription());
            pstmt.setString(3, equipment.getCategory().name());
            pstmt.setString(4, equipment.getStatus().name());
            pstmt.setDouble(5, equipment.getDailyRate());
            pstmt.setString(6, equipment.getEquipmentId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteEquipment(String equipmentId) {
        String sql = "DELETE FROM equipment WHERE equipment_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, equipmentId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateStatus(String equipmentId, EquipmentStatus status) {
        String sql = "UPDATE equipment SET status = ? WHERE equipment_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status.name());
            pstmt.setString(2, equipmentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Equipment mapResultSetToEquipment(ResultSet rs) throws SQLException {
        String equipmentId = rs.getString("equipment_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        EquipmentCategory category = EquipmentCategory.valueOf(rs.getString("category"));
        EquipmentStatus status = EquipmentStatus.valueOf(rs.getString("status"));
        double dailyRate = rs.getDouble("daily_rate");

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
                equipment = new Electronics(equipmentId, name, description, dailyRate, pricing, penalty);
        }
        equipment.setStatus(status);
        return equipment;
    }
}
