package dk.sdu.st4.agv.service;

import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IAgv;
import dk.sdu.st4.common.services.IAgvRegistry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AgvRegistry implements IAgvRegistry {

    private final Queue<String>     pending   = new ConcurrentLinkedQueue<>();
    private final Map<String, IAgv> services  = new ConcurrentHashMap<>();
    private final Queue<String>     available = new ConcurrentLinkedQueue<>();
    private final Set<String>       active    = ConcurrentHashMap.newKeySet();

    @Override
    public void loadFromDb() {
        String sql = "SELECT DISTINCT m.serial_no FROM machines m " +
                     "INNER JOIN line_machines lm ON m.serial_no = lm.serial_no " +
                     "WHERE m.type = 'AGV'";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String sn = rs.getString("serial_no");
                if (!services.containsKey(sn)) pending.add(sn);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load AGVs from DB", e);
        }
    }

    @Override
    public void connectNext() {
        String sn = pending.poll();
        if (sn == null) return;
        connect(sn);
    }

    @Override
    public void connect(String sn) {
        if (services.containsKey(sn)) return;
        String sql = "SELECT base_url FROM machines WHERE serial_no = ?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, sn);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { System.err.println("[AgvRegistry] " + sn + " not found in DB"); return; }
                AgvConnect connect = new AgvConnect(rs.getString("base_url"));
                connect.connectMachine(sn);
                services.put(sn, connect.getModel().client);
                available.add(sn);
            }
        } catch (Exception e) {
            System.err.println("[AgvRegistry] Failed to connect " + sn + ": " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getMachinesStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, IAgv> e : services.entrySet()) {
            String sn = e.getKey();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("serialNo", sn);
            m.put("poolStatus", active.contains(sn) ? "active" : "idle");
            try {
                var status = e.getValue().getStatus();
                m.put("agvState", status.getState() != null ? status.getState().name() : "Unknown");
                m.put("battery",  status.getBattery());
                m.put("program",  status.getProgramName() != null ? status.getProgramName() : "");
            } catch (Exception ex) {
                m.put("agvState", "Unknown");
                m.put("battery",  null);
                m.put("program",  "");
            }
            result.add(m);
        }
        return result;
    }

    @Override
    public void disconnect(String sn) {
        pending.remove(sn);
        available.remove(sn);
        active.remove(sn);
        services.remove(sn);
    }

    @Override
    public IAgv acquire() {
        String sn = available.poll();
        if (sn == null) return null;
        active.add(sn);
        return services.get(sn);
    }

    @Override
    public void release(IAgv agv) {
        services.entrySet().stream()
                .filter(e -> e.getValue() == agv)
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(sn -> {
                    if (active.remove(sn)) available.add(sn);
                });
    }
}
