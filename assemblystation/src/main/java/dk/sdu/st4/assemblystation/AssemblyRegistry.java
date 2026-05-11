package dk.sdu.st4.assemblystation;

import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IAssembly;
import dk.sdu.st4.common.services.IAssemblyRegistry;
import dk.sdu.st4.common.services.IConnect;

import java.sql.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AssemblyRegistry implements IAssemblyRegistry {

    private final Queue<IConnect>        pending   = new LinkedList<>();
    private final Map<String, IAssembly> services  = new HashMap<>();
    private final Queue<String>          available = new LinkedList<>();
    private final java.util.Set<String>  active    = new java.util.HashSet<>();

    @Override
    public void loadFromDb() throws Exception {
        String sql = "SELECT serial_no FROM machines WHERE type = 'ASSEMBLY_STATION'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String serialNo = rs.getString("serial_no");
                AssemblyConnect connect = new AssemblyConnect();
                connect.setMachineId(serialNo);
                pending.add(connect);
            }
        }
    }

    @Override
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

    @Override
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

    @Override
    public IConnect connectNext() throws ExecutionException, InterruptedException {
        if (pending.isEmpty()) return null;
        IConnect machine = pending.poll();
        machine.connectMachine(machine.getMachineId()).get();
        String sn = machine.getMachineId();
        AssemblyController controller = new AssemblyController(((AssemblyConnect) machine).getModel());
        services.put(sn, controller);
        available.add(sn);
        return machine;
    }

    @Override
    public IAssembly acquire() {
        String sn = available.isEmpty() ? null : ((LinkedList<String>) available).poll();
        if (sn == null) return null;
        active.add(sn);
        return services.get(sn);
    }

    @Override
    public void release(IAssembly assembly) {
        services.entrySet().stream()
                .filter(e -> e.getValue() == assembly)
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(sn -> {
                    if (active.remove(sn)) available.add(sn);
                });
    }
}
