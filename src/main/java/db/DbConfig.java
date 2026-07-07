package db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads the {@code config.properties} file from the classpath and
 * exposes the settings through typed accessors. Throws a clear
 * {@link IllegalStateException} if the file is missing so the GUI
 * can show a friendly error at startup.
 */
public class DbConfig {

    private static final String FILE = "/config.properties";

    private final Properties props = new Properties();

    public DbConfig() {
        try (InputStream in = DbConfig.class.getResourceAsStream(FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                    FILE + " not found on the classpath. "
                  + "Place it in src/main/resources/.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + FILE, e);
        }
    }

    public String host()                  { return props.getProperty("db.host", "localhost"); }
    public int    port()                  { return Integer.parseInt(props.getProperty("db.port", "3306")); }
    public String database()              { return props.getProperty("db.database", "smart_rental"); }
    public String user()                  { return props.getProperty("db.user", "root"); }
    public String password()              { return props.getProperty("db.password", ""); }
    public boolean useSSL()               { return Boolean.parseBoolean(props.getProperty("db.useSSL", "false")); }
    public boolean allowPublicKeyRetrieval() {
        return Boolean.parseBoolean(props.getProperty("db.allowPublicKeyRetrieval", "true"));
    }
}