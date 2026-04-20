package dk.sdu.st4.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static DBConnection instance;

    private static final String URL = "jdbc:postgresql://localhost:5432/skateboardas";
    private static final String USER = "skateboardas";
    private static final String PASSWORD = "skateboardas";

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