package dk.sdu.st4.agv.service;

import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IAgv;
import dk.sdu.st4.common.services.spi.IAgvFactory;
import dk.sdu.st4.common.services.IAgvRegistry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class AgvRegistry implements IAgvRegistry {

    private final Queue<String>     pending        = new ConcurrentLinkedQueue<>();
    private final Map<String, IAgv> services       = new ConcurrentHashMap<>();
    private final Queue<String>     available      = new ConcurrentLinkedQueue<>();
    private final Set<String>       active         = ConcurrentHashMap.newKeySet();
    private final Map<String, Map<String, Object>> telemetryCache = new ConcurrentHashMap<>();

    private final Map<String, IAgvFactory> factories =
            ServiceLoader.load(IAgvFactory.class).stream()
                    .map(ServiceLoader.Provider::get)
                    .collect(Collectors.toMap(IAgvFactory::variant, f -> f));

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
        String sql = "SELECT base_url, protocol FROM machines WHERE serial_no = ?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, sn);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) { System.err.println("[AgvRegistry] " + sn + " not found in DB"); return; }
                String baseUrl  = rs.getString("base_url");
                String protocol = rs.getString("protocol");
                IAgvFactory factory = factories.get(protocol);
                if (factory == null) {
                    System.err.println("[AgvRegistry] No IAgvFactory for protocol '" + protocol
                            + "' (serial " + sn + ") — vendor module missing in mods-mvn?");
                    return;
                }
                IAgv agv = factory.create(sn, baseUrl);
                services.put(sn, agv);
                available.add(sn);
                startTelemetryPoller(sn, agv);
            }
        } catch (Exception e) {
            System.err.println("[AgvRegistry] Failed to connect " + sn + ": " + e.getMessage());
        }
    }

    // Background thread per AGV — refreshes telemetry cache every 1 s independently of UI polls.
    // getMachinesStatus() reads the cache and is always instant (no blocking HTTP calls).
    private void startTelemetryPoller(String sn, IAgv agv) {
        Thread t = new Thread(() -> {
            while (services.containsKey(sn)) {
                try {
                    var status = agv.getStatus();
                    Map<String, Object> snap = new LinkedHashMap<>();
                    snap.put("agvState", status.getState() != null ? status.getState().name() : "Unknown");
                    snap.put("battery",  status.getBattery());
                    snap.put("program",  status.getProgramName() != null ? status.getProgramName() : "");
                    telemetryCache.put(sn, snap);
                } catch (Exception ignored) { /* keep stale cache on error */ }
                try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
            }
            telemetryCache.remove(sn);
        }, "agv-telem-" + sn);
        t.setDaemon(true);
        t.start();
    }

    @Override
    public List<Map<String, Object>> getMachinesStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String sn : services.keySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("serialNo",   sn);
            m.put("poolStatus", active.contains(sn) ? "active" : "idle");
            Map<String, Object> telem = telemetryCache.get(sn);
            if (telem != null) {
                m.putAll(telem);
            } else {
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
        services.remove(sn);   // poller thread exits on next iteration when services no longer contains sn
        telemetryCache.remove(sn);
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
