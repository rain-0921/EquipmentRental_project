package rental.repo;

import rental.model.rental.Rental;
import rental.model.rental.RentalStatus;
import rental.model.user.User;
import rental.model.user.UserRole;
import rental.model.user.StudentUser;
import rental.model.user.FinalYearStudentUser;
import rental.model.user.StaffUser;
import rental.model.equipment.Equipment;
import rental.model.equipment.EquipmentCategory;
import rental.model.equipment.EquipmentStatus;
import rental.model.pricing.StandardPricing;
import rental.model.penalty.DamagePenalty;
import rental.model.penalty.DamageSeverity;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RentalRepository {
    private static RentalRepository instance;
    private final DatabaseManager db;

    private RentalRepository() {
        this.db = DatabaseManager.getInstance();
    }

    public static RentalRepository getInstance() {
        if (instance == null) {
            instance = new RentalRepository();
        }
        return instance;
    }

    public String generateRentalId() {
        String sql = "UPDATE counters SET counter_value = counter_value + 1 WHERE counter_name = 'rental_counter'";
        String selectSql = "SELECT counter_value FROM counters WHERE counter_name = 'rental_counter'";
        
        try (Connection conn = db.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            ResultSet rs = stmt.executeQuery(selectSql);
            if (rs.next()) {
                int counter = rs.getInt("counter_value");
                return String.format("R-%d-%04d", LocalDate.now().getYear(), counter);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return String.format("R-%d-0001", LocalDate.now().getYear());
    }

    public void addRental(Rental rental) {
        String sql = "INSERT INTO rentals (rental_id, user_id, equipment_id, rental_days, rental_date, due_date, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rental.getRentalId());
            pstmt.setString(2, rental.getUser().getUserId());
            pstmt.setString(3, rental.getEquipment().getEquipmentId());
            pstmt.setInt(4, rental.getRentalDays());
            pstmt.setDate(5, java.sql.Date.valueOf(rental.getRentalDate()));
            pstmt.setDate(6, java.sql.Date.valueOf(rental.getDueDate()));
            pstmt.setString(7, rental.getStatus().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Rental getRental(String rentalId) {
        String sql = "SELECT r.*, u.name as user_name, u.password, u.role as user_role, " +
                     "e.name as equipment_name, e.description, e.category, e.daily_rate " +
                     "FROM rentals r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                     "WHERE r.rental_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rentalId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToRental(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Rental> getAllRentals() {
        List<Rental> rentals = new ArrayList<>();
        String sql = "SELECT r.*, u.name as user_name, u.password, u.role as user_role, " +
                     "e.name as equipment_name, e.description, e.category, e.daily_rate, e.status as equipment_status " +
                     "FROM rentals r " +
                     "JOIN users u ON r.user_id = u.user_id " +
                     "JOIN equipment e ON r.equipment_id = e.equipment_id";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rentals.add(mapResultSetToRental(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rentals;
    }

    public List<Rental> getRentalsByUser(User user) {
        return getAllRentals().stream()
            .filter(r -> r.getUser().getUserId().equals(user.getUserId()))
            .collect(Collectors.toList());
    }

    public List<Rental> getActiveRentalsByUser(User user) {
        return getAllRentals().stream()
            .filter(r -> r.getUser().getUserId().equals(user.getUserId()) &&
                        r.getStatus() == RentalStatus.ACTIVE)
            .collect(Collectors.toList());
    }

    public void updateRental(Rental rental) {
        String sql = "UPDATE rentals SET status = ?, actual_return_date = ?, reported_severity = ?, final_severity = ? WHERE rental_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, rental.getStatus().name());
            if (rental.getActualReturnDate() != null) {
                pstmt.setDate(2, java.sql.Date.valueOf(rental.getActualReturnDate()));
            } else {
                pstmt.setNull(2, Types.DATE);
            }
            pstmt.setString(3, rental.getReportedSeverity() != null ? rental.getReportedSeverity().name() : null);
            pstmt.setString(4, rental.getFinalSeverity() != null ? rental.getFinalSeverity().name() : null);
            pstmt.setString(5, rental.getRentalId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean hasActiveRentalForEquipment(String equipmentId) {
        String sql = "SELECT COUNT(*) FROM rentals WHERE equipment_id = ? AND status = 'ACTIVE'";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, equipmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasPendingReturnRequest(String equipmentId) {
        String sql = "SELECT COUNT(*) FROM rentals WHERE equipment_id = ? AND status = 'RETURN_REQUESTED'";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, equipmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean hasActiveRentalsForUser(String userId) {
        String sql = "SELECT COUNT(*) FROM rentals WHERE user_id = ? AND status IN ('ACTIVE', 'RETURN_REQUESTED')";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Rental mapResultSetToRental(ResultSet rs) throws SQLException {
        String rentalId = rs.getString("rental_id");
        String userId = rs.getString("user_id");
        String userName = rs.getString("user_name");
        String password = rs.getString("password");
        UserRole userRole = UserRole.valueOf(rs.getString("user_role"));
        
        User user;
        switch (userRole) {
            case STUDENT:
                user = new StudentUser(userId, userName, password);
                break;
            case FINAL_YEAR_STUDENT:
                user = new FinalYearStudentUser(userId, userName, password);
                break;
            case STAFF:
                user = new StaffUser(userId, userName, password);
                break;
            default:
                user = new StudentUser(userId, userName, password);
        }

        String equipmentId = rs.getString("equipment_id");
        String equipmentName = rs.getString("equipment_name");
        String description = rs.getString("description");
        EquipmentCategory category = EquipmentCategory.valueOf(rs.getString("category"));
        double dailyRate = rs.getDouble("daily_rate");
        
        Equipment equipment;
        switch (category) {
            case ELECTRONICS:
                equipment = new rental.model.equipment.Electronics(
                    equipmentId, equipmentName, description, dailyRate, 
                    new StandardPricing(), new DamagePenalty());
                break;
            case MEDIA:
                equipment = new rental.model.equipment.MediaEquipment(
                    equipmentId, equipmentName, description, dailyRate, 
                    new StandardPricing(), new DamagePenalty());
                break;
            case LABORATORY:
                equipment = new rental.model.equipment.LaboratoryEquipment(
                    equipmentId, equipmentName, description, dailyRate, 
                    new StandardPricing(), new DamagePenalty());
                break;
            default:
                equipment = new rental.model.equipment.Electronics(
                    equipmentId, equipmentName, description, dailyRate, 
                    new StandardPricing(), new DamagePenalty());
        }

        java.sql.Date rentalDate = rs.getDate("rental_date");
        java.sql.Date dueDate = rs.getDate("due_date");
        java.sql.Date actualReturnDate = rs.getDate("actual_return_date");
        
        Rental rental = new Rental(rentalId, user, equipment, rs.getInt("rental_days"));
        rental.setStatus(RentalStatus.valueOf(rs.getString("status")));
        
        if (rentalDate != null) {
            rental.setRentalDate(rentalDate.toLocalDate());
        }
        if (dueDate != null) {
            rental.setDueDate(dueDate.toLocalDate());
        }
        if (actualReturnDate != null) {
            rental.setActualReturnDate(actualReturnDate.toLocalDate());
        }

        String reportedSeverity = rs.getString("reported_severity");
        if (reportedSeverity != null) {
            rental.setReportedSeverity(DamageSeverity.valueOf(reportedSeverity));
        }

        String finalSeverity = rs.getString("final_severity");
        if (finalSeverity != null) {
            rental.setFinalSeverity(DamageSeverity.valueOf(finalSeverity));
        }

        return rental;
    }
}
