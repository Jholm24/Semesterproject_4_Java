package dk.sdu.st4.agv.service;

import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IAgv;
import dk.sdu.st4.common.services.IAgvRegistry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        String sql = "SELECT serial_no FROM machines WHERE type = 'AGV'";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pending.add(rs.getString("serial_no"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load AGVs from DB", e);
        }
    }

    @Override
    public void connectNext() {
        String sn = pending.poll();
        if (sn == null) return;

        String sql = "SELECT base_url FROM machines WHERE serial_no = ?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, sn);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("AGV " + sn + " not found in DB");
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
