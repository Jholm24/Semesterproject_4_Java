package dk.sdu.st4.assemblystation;

import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IAssembly;
import dk.sdu.st4.common.services.IAssemblyRegistry;
import dk.sdu.st4.common.services.IConnect;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class AssemblyRegistry implements IAssemblyRegistry {

    private final Queue<IConnect>        pending   = new ConcurrentLinkedQueue<>();
    private final Map<String, IAssembly> services  = new ConcurrentHashMap<>();
    private final Queue<String>          available = new ConcurrentLinkedQueue<>();
    private final java.util.Set<String>  active    = ConcurrentHashMap.newKeySet();

    @Override
    public void loadFromDb() throws Exception {
        String sql = "SELECT DISTINCT m.serial_no FROM machines m " +
                     "INNER JOIN line_machines lm ON m.serial_no = lm.serial_no " +
                     "WHERE m.type = 'ASSEMBLY_STATION'";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String serialNo = rs.getString("serial_no");
                if (!services.containsKey(serialNo)) {
                    AssemblyConnect connect = new AssemblyConnect();
                    connect.setMachineId(serialNo);
                    pending.add(connect);
                }
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
    public void connect(String sn) throws Exception {
        if (services.containsKey(sn)) return;
        AssemblyConnect connect = new AssemblyConnect();
        connect.setMachineId(sn);
        connect.connectMachine(sn).get();
        AssemblyController controller = new AssemblyController(connect.getModel());
        services.put(sn, controller);
        available.add(sn);
    }

    @Override
    public void disconnect(String sn) {
        available.remove(sn);
        active.remove(sn);
        services.remove(sn);
        pending.removeIf(c -> sn.equals(c.getMachineId()));
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
    public List<Map<String, Object>> getMachinesStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, IAssembly> e : services.entrySet()) {
            String sn = e.getKey();
            IAssembly asm = e.getValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("serialNo",  sn);
            m.put("poolStatus", active.contains(sn) ? "active" : "idle");
            try { m.put("state",   asm.getStatus()); }   catch (Exception ex) { m.put("state",   0);     }
            try { m.put("healthy", asm.getHealth()); }   catch (Exception ex) { m.put("healthy", null);  }
            try { m.put("operationId", asm.getOperation()); } catch (Exception ex) { m.put("operationId", -1); }
            m.put("lastOperationId", asm.getLastOperationId());
            result.add(m);
        }
        return result;
    }

    @Override
    public IAssembly acquire() {
        String sn = available.poll();
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
