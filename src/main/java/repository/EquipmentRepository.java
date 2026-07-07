package repository;

import model.equipment.EquipmentCategory;
import model.equipment.EquipmentItem;
import model.equipment.EquipmentItemFactory;
import model.equipment.EquipmentStatus;
import model.pricing.PricingPolicy;
import model.pricing.PricingPolicyFactory;

import db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the {@code equipment} table. Re-hydrates the
 * {@link EquipmentItem} (Bridge abstraction) and its
 * {@link PricingPolicy} (Bridge implementor) via the
 * {@link PricingPolicyFactory} - this is exactly where the
 * Bridge is bound together.
 */
public class EquipmentRepository {

    private static final String SELECT_ALL =
        "SELECT equipment_id, name, category, daily_rate, pricing_strategy, available "
      + "FROM equipment ORDER BY category, equipment_id";

    private static final String UPDATE_AVAILABILITY =
        "UPDATE equipment SET available = ? WHERE equipment_id = ?";

    public List<EquipmentItem> findAll() throws SQLException {
        List<EquipmentItem> items = new ArrayList<>();
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        }
        return items;
    }

    public void setAvailability(String equipmentId, boolean available) throws SQLException {
        try (Connection c = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(UPDATE_AVAILABILITY)) {
            ps.setBoolean(1, available);
            ps.setString(2, equipmentId);
            ps.executeUpdate();
        }
    }

    private EquipmentItem mapRow(ResultSet rs) throws SQLException {
        String id          = rs.getString("equipment_id");
        String name        = rs.getString("name");
        EquipmentCategory cat = EquipmentCategory.valueOf(rs.getString("category"));
        double rate        = rs.getDouble("daily_rate");
        String stratKey    = rs.getString("pricing_strategy");
        PricingPolicy policy = PricingPolicyFactory.getInstance().fromKey(stratKey);
        EquipmentStatus status = rs.getBoolean("available")
            ? EquipmentStatus.AVAILABLE
            : EquipmentStatus.RENTED;
        return EquipmentItemFactory.create(id, name, cat, rate, policy, status);
    }
}