package dk.sdu.st4.app.Registries;

import dk.sdu.st4.warehouse.service.WarehouseService;
import dk.sdu.st4.common.db.DbMachineConnect;
import dk.sdu.st4.common.services.IWarehouse;

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
        super("Warehouse");
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

    // ── DB bootstrap ────────────────────────────────────────────────────────

    /** Connects every Warehouse row found in the machines table. */
    public void loadFromDb() {
        List<String> serialNumbers = DbMachineConnect.getMachinesByType("Warehouse");
        for (String sn : serialNumbers) {
            connectMachine(sn);
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
