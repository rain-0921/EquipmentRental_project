package gui;

import db.DatabaseManager;
import service.AuthService;
import model.user.User;

import java.util.Optional;

/**
 * Quick smoke probe: verifies the DB connection, then attempts
 * authentication with one seed user and prints the result.
 * Exits 0 on success, 1 on any failure.
 */
public class SmokeTest {

    private static boolean fail = false;

    private static void check(String name, boolean cond, String detail) {
        if (cond) {
            System.out.println("  ok: " + name + (detail.isEmpty() ? "" : " (" + detail + ")"));
        } else {
            System.out.println("  FAIL: " + name + (detail.isEmpty() ? "" : " (" + detail + ")"));
            fail = true;
        }
    }

    public static void main(String[] args) {
        try {
            boolean ok = DatabaseManager.getInstance().testConnection();
            System.out.println("DB connection: " + (ok ? "OK" : "FAIL"));
            if (!ok) { System.exit(1); }

            Optional<User> alice = new AuthService().login(
                "alice.tan@mmu.edu.my", "Student@123");
            System.out.println("Alice login: " + (alice.isPresent()
                ? "OK -> " + alice.get().getFullName() + " (" + alice.get().getType() + ")"
                : "FAIL"));

            // Alice is a regular student -> 0% discount, not final-year.
            if (alice.isPresent()) {
                User u = alice.get();
                check("Alice discount = 0.0", Math.abs(u.getDiscountRate() - 0.0) < 1e-9,
                    "actual=" + u.getDiscountRate());
                check("Alice is NOT final year", !u.isFinalYear(),
                    "isFinalYear=" + u.isFinalYear());
            }

            Optional<User> bad = new AuthService().login(
                "alice.tan@mmu.edu.my", "wrong-password");
            System.out.println("Wrong password: " + (bad.isEmpty() ? "REJECTED (correct)" : "ACCEPTED (bug!)"));

            Optional<User> lim = new AuthService().login(
                "dr.lim@mmu.edu.my", "Staff@1234");
            System.out.println("Dr Lim login: " + (lim.isPresent()
                ? "OK -> " + lim.get().getFullName() + " (" + lim.get().getType() + ")"
                : "FAIL"));

            // Dr Lim is staff -> 20% discount (per Staff policy).
            if (lim.isPresent()) {
                User u = lim.get();
                check("Dr Lim discount = 0.20", Math.abs(u.getDiscountRate() - 0.20) < 1e-9,
                    "actual=" + u.getDiscountRate());
            }

            Optional<User> chia = new AuthService().login(
                "chia.wei@mmu.edu.my", "Chia@1234");
            System.out.println("Chia Wei login: " + (chia.isPresent()
                ? "OK -> " + chia.get().getFullName() + " (" + chia.get().getType() + ")"
                : "FAIL"));

            // Chia Wei is a final-year student -> 15% discount.
            if (chia.isPresent()) {
                User u = chia.get();
                check("Chia Wei discount = 0.15", Math.abs(u.getDiscountRate() - 0.15) < 1e-9,
                    "actual=" + u.getDiscountRate());
                check("Chia Wei IS final year", u.isFinalYear(),
                    "isFinalYear=" + u.isFinalYear());
            }

            System.out.println("---");
            System.out.println(fail ? "SmokeTest: FAILURES" : "SmokeTest: all checks passed");
            System.exit(fail ? 1 : 0);
        } catch (Throwable t) {
            System.err.println("ERROR: " + t);
            t.printStackTrace();
            System.exit(1);
        }
    }
}