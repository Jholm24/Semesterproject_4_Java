package dk.sdu.st4.common.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MachineRepository {

    private static final String URL;
    private static final String USER;
    private static final String PASS;

    static {
        Properties props = new Properties();
        String path = Paths.get("db.properties").toAbsolutePath().toString();
        try (FileInputStream fis = new FileInputStream(path)) {
            props.load(fis);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("db.properties not found at " + path
                + " — copy db.properties.example and fill in your credentials.");
        }
        URL  = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASS = props.getProperty("db.password");
    }

    public List<MachineConfig> findAll() throws SQLException {
        return query("SELECT id, type, name, base_url FROM machines ORDER BY type, id", null);
    }

    public List<MachineConfig> findByType(String type) throws SQLException {
        return query("SELECT id, type, name, base_url FROM machines WHERE type = ? ORDER BY id", type);
    }

    private List<MachineConfig> query(String sql, String typeParam) throws SQLException {
        List<MachineConfig> result = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (typeParam != null) ps.setString(1, typeParam);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new MachineConfig(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("name"),
                        rs.getString("base_url")
                    ));
                }
            }
        }
        return result;
    }
}
