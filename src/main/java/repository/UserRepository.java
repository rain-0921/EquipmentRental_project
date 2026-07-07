package repository;

import model.user.AuthenticatedUser;
import model.user.User;
import model.user.UserFactory;
import model.user.UserType;

import db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD operations for the {@code user} table. */
public class UserRepository {

    private static final String SELECT_ALL =
        "SELECT user_id, full_name, email, user_type, discount_rate, is_final_year, created_at "
      + "FROM user ORDER BY user_id";

    private static final String SELECT_BY_ID =
        "SELECT user_id, full_name, email, user_type, discount_rate, is_final_year, created_at "
      + "FROM user WHERE user_id = ?";

    private static final String SELECT_BY_EMAIL_WITH_HASH =
        "SELECT user_id, full_name, email, user_type, discount_rate, is_final_year, created_at, password_hash "
      + "FROM user WHERE LOWER(email) = ?";

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        }
        return users;
    }

    public Optional<User> findById(String userId) throws SQLException {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_ID)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /** Look up a user by email, including the stored password hash.
     *  Empty if the user does not exist or has no password set. */
    public Optional<AuthenticatedUser> findByEmail(String emailLower) throws SQLException {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_EMAIL_WITH_HASH)) {
            ps.setString(1, emailLower);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                String id      = rs.getString("user_id");
                String name    = rs.getString("full_name");
                String mail    = rs.getString("email");
                UserType type  = UserType.valueOf(rs.getString("user_type"));
                double disc    = rs.getDouble("discount_rate");
                boolean fy     = rs.getBoolean("is_final_year");
                var createdAt  = rs.getTimestamp("created_at").toLocalDateTime();
                String hash    = rs.getString("password_hash");
                if (hash == null) return Optional.empty();
                return Optional.of(UserFactory.createWithCredentials(
                    id, name, mail, type, disc, fy, createdAt, hash));
            }
        }
    }

    /** Inserts a new user and returns the freshly-created row. */
    public User insert(User u) throws SQLException {
        String sql =
            "INSERT INTO user (user_id, full_name, email, user_type, discount_rate, is_final_year) "
          + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUserId());
            ps.setString(2, u.getFullName());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getType().name());
            ps.setDouble(5, u.getDiscountRate());
            ps.setBoolean(6, u.isFinalYear());
            ps.executeUpdate();
        }
        return u;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        String id      = rs.getString("user_id");
        String name    = rs.getString("full_name");
        String email   = rs.getString("email");
        UserType type  = UserType.valueOf(rs.getString("user_type"));
        double disc    = rs.getDouble("discount_rate");
        boolean fy     = rs.getBoolean("is_final_year");
        var createdAt  = rs.getTimestamp("created_at").toLocalDateTime();
        return UserFactory.create(id, name, email, type, disc, fy, createdAt);
    }
}
