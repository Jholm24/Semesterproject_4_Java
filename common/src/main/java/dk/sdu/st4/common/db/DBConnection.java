package dk.sdu.st4.common.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBConnection {

    private static final Map<String, String> DOT_ENV = loadDotEnv();

    private static final String URL      = env("DB_URL");
    private static final String USER     = env("DB_USER");
    private static final String PASSWORD = env("DB_PASSWORD");

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Kunne ikke oprette forbindelse til databasen", e);
        }
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Kunne ikke genoprette forbindelse til databasen", e);
        }
        return connection;
    }

    // ── .env loader ──────────────────────────────────────────────────────────

    private static Map<String, String> loadDotEnv() {
        Map<String, String> props = new HashMap<>();
        Path path = findDotEnv();
        if (path == null) return props;
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                props.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
            }
        } catch (IOException e) {
            // .env is optional — silently skip
        }
        return props;
    }

    /** Walks up from the working directory until a .env file is found (up to 4 levels). */
    private static Path findDotEnv() {
        Path dir = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath();
        for (int i = 0; i < 4; i++) {
            Path candidate = dir.resolve(".env");
            if (Files.exists(candidate)) return candidate;
            Path parent = dir.getParent();
            if (parent == null) break;
            dir = parent;
        }
        return null;
    }

    /** OS environment takes precedence over .env file. */
    private static String env(String key) {
        String v = System.getenv(key);
        return v != null ? v : DOT_ENV.getOrDefault(key, "");
    }
}
