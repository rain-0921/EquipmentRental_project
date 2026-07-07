import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class GenHashes {
    static String hash(char[] pw, byte[] salt, int iter) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(pw, salt, iter, 256);
        byte[] k = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(salt) + ":" + iter + ":" + Base64.getEncoder().encodeToString(k);
    }
    public static void main(String[] a) throws Exception {
        String[][] users = {
            {"U001","Alice Tan","alice.tan@mmu.edu.my","STUDENT","Student@123"},
            {"U002","Bob Lee","bob.lee@mmu.edu.my","STUDENT","Bob@12345"},
            {"U003","Dr. Lim","dr.lim@mmu.edu.my","STAFF","Staff@1234"},
            {"U004","Chia Wei","chia.wei@mmu.edu.my","STUDENT","Chia@1234"}
        };
        SecureRandom rng = new SecureRandom();
        for (String[] u : users) {
            byte[] salt = new byte[16];
            rng.nextBytes(salt);
            String h = hash(u[4].toCharArray(), salt, 100_000);
            System.out.println("UPDATE user SET password_hash='" + h + "' WHERE user_id='" + u[0] + "'; -- " + u[1] + " / " + u[4]);
        }
    }
}
