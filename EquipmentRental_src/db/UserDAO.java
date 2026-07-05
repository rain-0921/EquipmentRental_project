package db;

import model.Staff;
import model.Student;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public void insert(User u) {
        String sql = "INSERT INTO app_user (user_id, full_name, role, final_year) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET " +
                "full_name = EXCLUDED.full_name, role = EXCLUDED.role, " +
                "final_year = EXCLUDED.final_year";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUserId());
            ps.setString(2, u.getFullName());
            if (u instanceof Student student) {
                ps.setString(3, "STUDENT");
                ps.setBoolean(4, student.isFinalYear());
            } else if (u instanceof Staff) {
                ps.setString(3, "STAFF");
                ps.setNull(4, Types.BOOLEAN);
            } else {
                throw new IllegalArgumentException("Unknown user subtype: " + u.getClass());
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save user: " + ex.getMessage(), ex);
        }
    }

    public void delete(String userId) {
        String sql = "DELETE FROM app_user WHERE user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete user (they may still be referenced by rental history): " + ex.getMessage(), ex);
        }
    }

    public List<User> findAll() {
        List<User> result = new ArrayList<>();
        String sql = "SELECT user_id, full_name, role, final_year FROM app_user ORDER BY user_id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load users: " + ex.getMessage(), ex);
        }
        return result;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("user_id");
        String name = rs.getString("full_name");
        String role = rs.getString("role");
        if ("STAFF".equals(role)) {
            return new Staff(id, name);
        } else {
            boolean finalYear = rs.getBoolean("final_year");
            return new Student(id, name, finalYear);
        }
    }
}
