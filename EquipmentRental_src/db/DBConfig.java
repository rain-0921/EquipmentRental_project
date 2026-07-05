package db;

/**
 * Database connection configuration.
 *
 * SECURITY NOTE: The fallback values below embed a real Neon Postgres
 * credential for convenience during development. Before submitting this
 * assignment or pushing to any public repository, replace the fallback
 * password/URL with placeholders, or better, always set the DB_URL /
 * DB_USER / DB_PASSWORD environment variables and remove the fallback
 * entirely.
 */
public final class DBConfig {

    // Full JDBC URL (without user/password baked in — those are passed separately).
    private static final String DEFAULT_HOST =
            "ep-wandering-breeze-atpniv8r-pooler.c-9.us-east-1.aws.neon.tech";
    private static final String DEFAULT_DB = "neondb";
    private static final String DEFAULT_USER = "neondb_owner";
    private static final String DEFAULT_PASSWORD = "npg_5kZMAGCs6lHJ";

    public static String getUrl() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) return envUrl;
        return "jdbc:postgresql://" + DEFAULT_HOST + "/" + DEFAULT_DB
                + "?sslmode=require"
                + "&connectTimeout=10"   // fail fast instead of hanging forever
                + "&socketTimeout=15";
    }

    public static String getUser() {
        String envUser = System.getenv("DB_USER");
        return (envUser != null && !envUser.isBlank()) ? envUser : DEFAULT_USER;
    }

    public static String getPassword() {
        String envPass = System.getenv("DB_PASSWORD");
        return (envPass != null && !envPass.isBlank()) ? envPass : DEFAULT_PASSWORD;
    }

    private DBConfig() {}
}
