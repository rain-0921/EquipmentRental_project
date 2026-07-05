package db;

import java.sql.*;

/**
 * Hands out a monotonically increasing number per prefix (e.g. "S" for
 * Student, "T" for Staff) so user IDs are never recycled - even if the
 * user holding a given ID is later deleted. This keeps every ID that was
 * ever assigned unique for good, matching how real student/staff numbers
 * behave (a number is retired, not reissued).
 */
public class IdSequenceDAO {

    /**
     * Atomically returns the next number for the given prefix and advances
     * the counter, seeding it with {@code startIfMissing} the first time
     * this prefix is used (so it starts above any legacy/seeded IDs that
     * existed before this sequence table did).
     */
    public int getAndAdvance(String prefix, int startIfMissing) {
        String sql = "INSERT INTO id_sequence (prefix, next_value) VALUES (?, ?) " +
                "ON CONFLICT (prefix) DO UPDATE SET next_value = id_sequence.next_value + 1 " +
                "RETURNING next_value";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix);
            ps.setInt(2, startIfMissing);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to generate next ID: " + ex.getMessage(), ex);
        }
    }
}
