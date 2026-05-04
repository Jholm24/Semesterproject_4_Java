package dk.sdu.st4.app.Registries;

import dk.sdu.st4.warehouse.service.WarehouseService;
import dk.sdu.st4.common.db.DbMachineConnect;
import dk.sdu.st4.common.services.IWarehouse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WarehouseRegistry extends DbMachineConnect {

    private final Map<String, WarehouseService> services = new ConcurrentHashMap<>();
    private final Queue<String> available = new ConcurrentLinkedQueue<>();
    private final Set<String> active = ConcurrentHashMap.newKeySet();

    public WarehouseRegistry() {
        super("WAREHOUSE");
    }

    // ── DbMachineConnect hooks ───────────────────────────────────────────────

    @Override
    protected void onConnect(String serialNo, String baseUrl) {
        services.put(serialNo, new WarehouseService(baseUrl));
        available.add(serialNo);
    }

    @Override
    protected void onDisconnect(String serialNo) {
        services.remove(serialNo);
        available.remove(serialNo);
        active.remove(serialNo);
    }

    /** Returns serial numbers of all connected (idle) warehouses. */
    public Set<String> getAvailable() {
        return Set.copyOf(available);
    }

    /** Returns live status for every connected Warehouse: serialNumber, poolStatus, warehouseState. */
    public List<Map<String, Object>> getPoolInfo() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, WarehouseService> entry : services.entrySet()) {
            String sn = entry.getKey();
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("serialNumber", sn);
            info.put("poolStatus", active.contains(sn) ? "active" : "idle");
            try {
                info.put("warehouseState", entry.getValue().GetState(sn));
            } catch (Exception e) {
                info.put("warehouseState", -1);
            }
            result.add(info);
        }
        return result;
    }

    // ── DB bootstrap ────────────────────────────────────────────────────────

    /** Connects every WAREHOUSE row found in the machines table. */
    public void loadFromDb() {
        List<String> serialNumbers = DbMachineConnect.getMachinesByType("WAREHOUSE");
        for (String sn : serialNumbers) {
            try {
                connectMachine(sn);
            } catch (Exception e) {
                System.err.println("[WarehouseRegistry] Failed to connect " + sn + ": " + e.getMessage());
                Throwable cause = e.getCause();
                while (cause != null) {
                    System.err.println("  Caused by: " + cause.getClass().getName() + ": " + cause.getMessage());
                    cause = cause.getCause();
                }
            }
        }
    }

    // ── pool API ─────────────────────────────────────────────────────────────

    /** Takes an idle warehouse from the pool and marks it active. Returns null if none available. */
    public IWarehouse acquire() {
        String sn = available.poll();
        if (sn == null) return null;
        active.add(sn);
        return services.get(sn);
    }

    /** Returns a previously acquired warehouse back to the idle pool. */
    public void release(IWarehouse machine) {
        services.entrySet().stream()
                .filter(e -> e.getValue() == machine)
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(sn -> {
                    if (active.remove(sn)) {
                        available.add(sn);
                    }
                });
    }
}
