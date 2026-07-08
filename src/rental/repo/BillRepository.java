package rental.repo;

import rental.model.billing.Bill;
import rental.model.rental.Rental;
import rental.model.user.User;
import rental.model.user.UserRole;
import rental.model.user.UserFactory;
import rental.model.equipment.Equipment;
import rental.model.equipment.EquipmentCategory;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class BillRepository {
    private static BillRepository instance;
    private final DatabaseManager db;
    private final UserFactory userFactory;

    private BillRepository() {
        this.db = DatabaseManager.getInstance();
        this.userFactory = UserFactory.getInstance();
    }

    public static BillRepository getInstance() {
        if (instance == null) {
            instance = new BillRepository();
        }
        return instance;
    }

    public String generateBillId() {
        String sql = "UPDATE counters SET counter_value = counter_value + 1 WHERE counter_name = 'bill_counter'";
        String selectSql = "SELECT counter_value FROM counters WHERE counter_name = 'bill_counter'";

        try (Connection conn = db.getConnection()) {
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            ResultSet rs = stmt.executeQuery(selectSql);
            if (rs.next()) {
                int counter = rs.getInt("counter_value");
                Calendar cal = Calendar.getInstance();
                return String.format("B-%d-%04d", cal.get(Calendar.YEAR), counter);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        return String.format("B-%d-0001", cal.get(Calendar.YEAR));
    }

    public void addBill(Bill bill) {
        String sql = "INSERT INTO bills (bill_id, rental_id, equipment_name, renter_name, pricing_plan, subtotal, discount, late_penalty, damage_penalty, net_payable) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bill.getBillId());
            pstmt.setString(2, bill.getRentalId());
            pstmt.setString(3, bill.getEquipmentName());
            pstmt.setString(4, bill.getRenterName());
            pstmt.setString(5, bill.getPricingPlan());
            pstmt.setDouble(6, bill.getSubtotal());
            pstmt.setDouble(7, bill.getDiscount());
            pstmt.setDouble(8, bill.getLatePenalty());
            pstmt.setDouble(9, bill.getDamagePenalty());
            pstmt.setDouble(10, bill.getNetPayable());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Bill getBill(String billId) {
        String sql = "SELECT b.*, r.user_id, r.equipment_id FROM bills b " +
                     "JOIN rentals r ON b.rental_id = r.rental_id WHERE b.bill_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, billId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("user_id");
                String equipmentId = rs.getString("equipment_id");
                User user = getUserById(conn, userId);
                Equipment equipment = getEquipmentById(conn, equipmentId);
                return mapResultSetToBill(rs, user, equipment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Bill> getAllBills() {
        List<Bill> bills = new ArrayList<>();
        String billSql = "SELECT b.*, r.user_id, r.equipment_id FROM bills b " +
                     "JOIN rentals r ON b.rental_id = r.rental_id ORDER BY b.bill_id DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(billSql)) {
            Map<String, User> userCache = new HashMap<>();
            Map<String, Equipment> equipCache = new HashMap<>();
            while (rs.next()) {
                String userId = rs.getString("user_id");
                String equipmentId = rs.getString("equipment_id");
                if (!userCache.containsKey(userId)) {
                    userCache.put(userId, getUserById(conn, userId));
                }
                if (!equipCache.containsKey(equipmentId)) {
                    equipCache.put(equipmentId, getEquipmentById(conn, equipmentId));
                }
                bills.add(mapResultSetToBill(rs, userCache.get(userId), equipCache.get(equipmentId)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bills;
    }

    public List<Bill> getBillsByUser(User user) {
        return getAllBills().stream()
            .filter(b -> b.getRental() != null &&
                        b.getRental().getUser().getUserId().equals(user.getUserId()))
            .collect(Collectors.toList());
    }

    public List<Bill> getBillsByRenterName(String renterName) {
        return getAllBills().stream()
            .filter(b -> b.getRenterName().equals(renterName))
            .collect(Collectors.toList());
    }

    private Bill mapResultSetToBill(ResultSet rs, User user, Equipment equipment) throws SQLException {
        String billId = rs.getString("bill_id");
        String rentalId = rs.getString("rental_id");
        String equipmentName = rs.getString("equipment_name");
        String renterName = rs.getString("renter_name");
        String pricingPlan = rs.getString("pricing_plan");
        double subtotal = rs.getDouble("subtotal");
        double discount = rs.getDouble("discount");
        double latePenalty = rs.getDouble("late_penalty");
        double damagePenalty = rs.getDouble("damage_penalty");
        double netPayable = rs.getDouble("net_payable");

        Rental rental = new Rental(rentalId, user, equipment, 0);
        rental.setBill(new Bill());

        Bill bill = new Bill();
        bill.setBillId(billId);
        bill.setRental(rental);
        bill.setRentalId(rentalId);
        bill.setEquipmentName(equipmentName);
        bill.setRenterName(renterName);
        bill.setPricingPlan(pricingPlan);
        bill.setSubtotal(subtotal);
        bill.setDiscount(discount);
        bill.setLatePenalty(latePenalty);
        bill.setDamagePenalty(damagePenalty);
        bill.setNetPayable(netPayable);

        return bill;
    }

    private User getUserById(Connection conn, String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                UserRole role = UserRole.valueOf(rs.getString("role"));
                String name = rs.getString("name");
                String password = rs.getString("password");
                return userFactory.createUser(userId, name, password, role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userFactory.createUser(userId, "Unknown", "", UserRole.STUDENT);
    }

    private Equipment getEquipmentById(Connection conn, String equipmentId) {
        String sql = "SELECT * FROM equipment WHERE equipment_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, equipmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                EquipmentCategory category = EquipmentCategory.valueOf(rs.getString("category"));
                String name = rs.getString("name");
                String description = rs.getString("description");
                double dailyRate = rs.getDouble("daily_rate");

                switch (category) {
                    case ELECTRONICS:
                        return new rental.model.equipment.Electronics(equipmentId, name, description, dailyRate);
                    case MEDIA:
                        return new rental.model.equipment.MediaEquipment(equipmentId, name, description, dailyRate);
                    case LABORATORY:
                        return new rental.model.equipment.LaboratoryEquipment(equipmentId, name, description, dailyRate);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new rental.model.equipment.Electronics(equipmentId, "Unknown", "", 0);
    }
}
