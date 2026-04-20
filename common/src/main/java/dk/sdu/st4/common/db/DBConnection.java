package dk.sdu.st4.common.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;

    private static final String URL = System.getenv().getOrDefault("DB_URL", "change_me_in_Env");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "change_me_in_Env");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "change_me_in_Env");

    private Connection connection;

    private DBConnection() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Kunne ikke oprette forbindelse til databasen", e);
        }
    }

    public static DBConnection getInstance() {
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
}
