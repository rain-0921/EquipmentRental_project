package gui;

import db.DatabaseManager;
import model.billing.Bill;
import model.equipment.EquipmentItem;
import model.rental.Rental;
import model.user.User;
import repository.EquipmentRepository;
import repository.RentalRepository;
import repository.UserRepository;
import service.BillingService;
import service.RentalService;

import java.time.LocalDate;
import java.util.List;

/**
 * Integration smoke for the new return flow. Mirrors what a student
 * would do: rent something, see it in their list, return it, get a
 * bill. Run after a fresh DB seed.
 *
 * Exits 0 on success, 1 on any failure. Intentionally avoids
 * touching the GUI - we just exercise the services + the new
 * ReturnPanel's underlying BillRepository data path.
 */
public class ReturnFlowTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        try {
            boolean ok = DatabaseManager.getInstance().testConnection();
            if (!ok) { System.out.println("DB unreachable"); System.exit(1); }

            User chia = new UserRepository().findById("U004").orElseThrow(
                () -> new IllegalStateException("Chia Wei (U004) missing"));
            System.out.println("Loaded user: " + chia.getFullName()
                + " type=" + chia.getType()
                + " discount=" + chia.getDiscountRate()
                + " finalYear=" + chia.isFinalYear());

            // Make sure E001 is available before we rent. If the DB has
            // a stale ACTIVE rental for E001 from a previous test run,
            // close it first so we have a clean slate.
            EquipmentRepository equip = new EquipmentRepository();
            EquipmentItem e001 = equip.findAll().stream()
                .filter(e -> "E001".equals(e.getEquipmentId())).findFirst().orElseThrow();
            RentalRepository rentalRepo = new RentalRepository();
            rentalRepo.findActiveRentalForEquipment("E001").ifPresent(stale -> {
                try {
                    new BillingService().processReturn(
                        stale.getRentalId(), LocalDate.now(), model.rental.Rental.DamageLevel.NONE);
                    System.out.println("  cleaned up stale rental #" + stale.getRentalId());
                } catch (Exception ex) {
                    System.out.println("  stale-cleanup skipped: " + ex.getMessage());
                }
            });
            e001 = equip.findAll().stream()
                .filter(e -> "E001".equals(e.getEquipmentId())).findFirst().orElseThrow();
            if (!e001.isAvailable()) {
                System.out.println("E001 still unavailable after cleanup - aborting.");
                System.exit(1);
            }

            // 1. Rent E001 as Chia Wei.
            RentalService rentalService = new RentalService();
            Rental rental = rentalService.rentEquipment(
                "U004", "E001",
                LocalDate.now(), LocalDate.now().plusDays(3));
            check("rental created", rental.getRentalId() > 0);

            // 2. The new rental should appear in My Rentals query for U004.
            List<Rental> u4Rentals = rentalRepo.findByUser("U004");
            check("My Rentals includes the new rental",
                u4Rentals.stream().anyMatch(r -> r.getRentalId() == rental.getRentalId()));

            // 3. ReturnPanel would call findByUser too - confirm list is fresh.
            check("My Rentals shows ACTIVE status",
                u4Rentals.stream()
                    .filter(r -> r.getRentalId() == rental.getRentalId())
                    .allMatch(r -> r.getStatus() == Rental.Status.ACTIVE));

            // 4. Process the return. This is exactly what ReturnPanel does.
            BillingService billingService = new BillingService();
            Bill bill = billingService.processReturn(
                rental.getRentalId(), LocalDate.now(), Rental.DamageLevel.NONE);
            check("bill generated", bill.getBillId() > 0);
            check("net payable > 0", bill.getNetPayable() > 0.0);
            System.out.printf("  -> bill: base=%.2f discount=%.2f penalty=%.2f net=%.2f%n",
                bill.getBaseRentalFee(), bill.getDiscountAmount(),
                bill.getPenaltyAmount(), bill.getNetPayable());

            // 5. Equipment must be available again so it shows in the Rent picker.
            EquipmentItem after = equip.findAll().stream()
                .filter(e -> "E001".equals(e.getEquipmentId())).findFirst().orElseThrow();
            check("E001 is AVAILABLE after return", after.isAvailable());

            // 6. The rental row should now be RETURNED (no longer in the
            //    "active" list that ReturnPanel pulls).
            List<Rental> all = rentalRepo.findAll();
            Rental finalRental = all.stream()
                .filter(r -> r.getRentalId() == rental.getRentalId()).findFirst().orElseThrow();
            check("rental status -> RETURNED",
                finalRental.getStatus() == Rental.Status.RETURNED);
            check("rental return date set", finalRental.getReturnDate() != null);

            // 7. The bill preview calc is a pure function - calling it on the
            //    already-returned rental should still work (used by the panel).
            User chiaAgain = new UserRepository().findById("U004").orElseThrow();
            Bill preview = billingService.calculateBill(
                chiaAgain, e001, finalRental, LocalDate.now(), Rental.DamageLevel.NONE);
            check("bill preview matches persisted",
                Math.abs(preview.getNetPayable() - bill.getNetPayable()) < 0.01);

            // 8. Damage tiers - rent another item, return with each tier
            //    in turn, and verify the damage fee appears in the bill.
            for (Rental.DamageLevel tier : new Rental.DamageLevel[] {
                    Rental.DamageLevel.LIGHT,
                    Rental.DamageLevel.MODERATE,
                    Rental.DamageLevel.HEAVY }) {
                // Make sure E002 is available.
                final int[] eid = { 0 };
                rentalRepo.findActiveRentalForEquipment("E002").ifPresent(stale2 -> {
                    try { new BillingService().processReturn(
                        stale2.getRentalId(), LocalDate.now(), Rental.DamageLevel.NONE); }
                    catch (Exception ignored) {}
                });
                Rental r2 = rentalService.rentEquipment(
                    "U004", "E002",
                    LocalDate.now(), LocalDate.now().plusDays(2));
                Bill b2 = billingService.processReturn(
                    r2.getRentalId(), LocalDate.now(), tier);
                double expectedFee = tier.getFee();
                double expectedTotal = Math.round(
                    (b2.getBaseRentalFee() + expectedFee) * (1 - chiaAgain.getDiscountRate())
                    * 100) / 100.0;
                check(String.format("%s tier charged RM %.2f damage fee",
                        tier, expectedFee),
                    Math.abs(b2.getPenaltyAmount() - expectedFee) < 0.01);
                check(String.format("%s tier net ~ RM %.2f",
                        tier, expectedTotal),
                    Math.abs(b2.getNetPayable() - expectedTotal) < 0.05);
            }

            System.out.println("---");
            System.out.println("ReturnFlowTest: " + passed + " passed, " + failed + " failed");
            System.exit(failed == 0 ? 0 : 1);
        } catch (Throwable t) {
            System.err.println("ERROR: " + t);
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void check(String label, boolean cond) {
        if (cond) { passed++; System.out.println("  ok: " + label); }
        else      { failed++; System.out.println("FAIL: " + label); }
    }
}