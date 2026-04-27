package dk.sdu.st4.assemblystation;

import dk.sdu.st4.assemblystation.AssemblyController;
import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IConnect;

import java.sql.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.Map;

public class AssemblyRegistry {
    private static AssemblyRegistry instance;

    private final Queue<IConnect> available = new LinkedList<>();
    private final Map<String, IConnect> active = new HashMap<>();

    private AssemblyRegistry() {}

    public static synchronized AssemblyRegistry getInstance() {
        if (instance == null) {
            instance = new AssemblyRegistry();
        }
        return instance;
    }

    // Henter maskiner fra DB
    public void configure() throws Exception {
        String sql = "SELECT serial_no, base_url FROM machines WHERE type = 'assembly'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String serialNo = rs.getString("serial_no");
                String baseUrl = rs.getString("base_url");
                int port = Integer.parseInt(baseUrl); // base_url er porten for assembly
                AssemblyController controller = new AssemblyController(port);
                controller.setMachineId(serialNo);
                available.add(controller);
            }
        }
    }

    public void addMachine(String serialNumber, String type, String baseUrl) {
        String sql = "INSERT INTO machines (serial_no, type, base_url) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serialNumber);
            stmt.setString(2, type);
            stmt.setString(3, baseUrl);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeMachine(String serialNumber) {
        String sql = "DELETE FROM machines WHERE serial_no = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serialNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public IConnect connectNext() {
        if (available.isEmpty()) return null;
        IConnect machine = available.poll();
        machine.connectMachine(machine.getMachineId()); // bruger maskinens egen ID/port
        active.put("assembly-" + (active.size() + 1), machine);
        return machine;
    }

    public void disconnect(String key) {
        IConnect machine = active.remove(key);
        if (machine != null) {
            machine.disconnectMachine(machine.getMachineId()); // bruger maskinens egen ID/port
            available.add(machine);
        }
    }
}