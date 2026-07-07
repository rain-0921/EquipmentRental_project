package repository;

import model.billing.Bill;

import db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Persistence for the {@code bill} table. */
public class BillRepository {

    private static final String SELECT_ALL =
        "SELECT bill_id, rental_id, base_rental_fee, discount_amount, "
      + "       penalty_amount, net_payable, issued_at "
      + "FROM bill ORDER BY issued_at DESC";

    private static final String SELECT_BY_RENTAL =
        "SELECT bill_id, rental_id, base_rental_fee, discount_amount, "
      + "       penalty_amount, net_payable, issued_at "
      + "FROM bill WHERE rental_id = ?";

    private static final String INSERT =
        "INSERT INTO bill (rental_id, base_rental_fee, discount_amount, "
      + "                  penalty_amount, net_payable) "
      + "VALUES (?, ?, ?, ?, ?)";

    public List<Bill> findAll() throws SQLException {
        List<Bill> bills = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) bills.add(mapRow(rs));
        }
        return bills;
    }

    public Optional<Bill> findByRentalId(int rentalId) throws SQLException {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_RENTAL)) {
            ps.setInt(1, rentalId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Bill insert(Bill b) throws SQLException {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getRentalId());
            ps.setDouble(2, b.getBaseRentalFee());
            ps.setDouble(3, b.getDiscountAmount());
            ps.setDouble(4, b.getPenaltyAmount());
            ps.setDouble(5, b.getNetPayable());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Bill(
                        keys.getInt(1),
                        b.getRentalId(),
                        b.getBaseRentalFee(),
                        b.getDiscountAmount(),
                        b.getPenaltyAmount(),
                        b.getNetPayable(),
                        b.getIssuedAt());
                }
            }
        }
        return b;
    }

    private Bill mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("issued_at");
        return new Bill(
            rs.getInt("bill_id"),
            rs.getInt("rental_id"),
            rs.getDouble("base_rental_fee"),
            rs.getDouble("discount_amount"),
            rs.getDouble("penalty_amount"),
            rs.getDouble("net_payable"),
            ts != null ? ts.toLocalDateTime() : null);
    }
}