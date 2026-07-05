package db;

import model.Bill;
import model.Rental;

import java.sql.*;

public class BillDAO {

    public void insert(Bill bill) {
        String sql = "INSERT INTO bill (rental_id, base_fee, discount, penalty, strategy_used) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON CONFLICT (rental_id) DO UPDATE SET " +
                "base_fee = EXCLUDED.base_fee, discount = EXCLUDED.discount, " +
                "penalty = EXCLUDED.penalty, strategy_used = EXCLUDED.strategy_used";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bill.getRental().getRentalId());
            ps.setDouble(2, bill.getBaseFee());
            ps.setDouble(3, bill.getDiscount());
            ps.setDouble(4, bill.getPenalty());
            ps.setString(5, bill.getStrategyUsed());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to save bill: " + ex.getMessage(), ex);
        }
    }

    /** Loads and attaches the persisted bill (if any) onto the given Rental object. */
    public Bill findByRentalAndAttach(Rental rental) {
        String sql = "SELECT base_fee, discount, penalty, strategy_used FROM bill WHERE rental_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rental.getRentalId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Bill bill = new Bill(
                            rental,
                            rs.getDouble("base_fee"),
                            rs.getDouble("discount"),
                            rs.getDouble("penalty"),
                            rs.getString("strategy_used")
                    );
                    rental.attachExistingBill(bill);
                    return bill;
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load bill: " + ex.getMessage(), ex);
        }
        return null;
    }
}
