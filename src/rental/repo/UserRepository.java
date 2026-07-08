package rental.repo;

import rental.model.user.User;
import rental.model.user.UserRole;
import rental.model.user.UserStatus;
import rental.model.user.StudentUser;
import rental.model.user.FinalYearStudentUser;
import rental.model.user.StaffUser;

import java.sql.*;
import java.util.*;

public class UserRepository {
    private static UserRepository instance;
    private final DatabaseManager db;

    private UserRepository() {
        this.db = DatabaseManager.getInstance();
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    public void addUser(User user) {
        String sql = "INSERT INTO users (user_id, name, password, role, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getName());
            pstmt.setString(3, user.getPassword());
            pstmt.setString(4, user.getRole().name());
            pstmt.setString(5, user.getStatus().name());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUser(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<User> getAllActiveUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE status = 'ACTIVE'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void updateUser(User user) {
        String sql = "UPDATE users SET name = ?, password = ?, status = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getStatus().name());
            pstmt.setString(4, user.getUserId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteUser(String userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User findByUserIdAndPassword(String userId, String password) {
        String sql = "SELECT * FROM users WHERE user_id = ? AND password = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");
        String name = rs.getString("name");
        String password = rs.getString("password");
        UserRole role = UserRole.valueOf(rs.getString("role"));

        User user;
        switch (role) {
            case STUDENT:
                user = new StudentUser(userId, name, password);
                break;
            case FINAL_YEAR_STUDENT:
                user = new FinalYearStudentUser(userId, name, password);
                break;
            case STAFF:
                user = new StaffUser(userId, name, password);
                break;
            default:
                user = new StudentUser(userId, name, password);
        }

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            user.setStatus(UserStatus.valueOf(statusStr));
        }

        return user;
    }
}
