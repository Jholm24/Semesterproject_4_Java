package dk.sdu.st4.app.Registries;

import dk.sdu.st4.assemblystation.AssemblyServiceImpl;
import dk.sdu.st4.common.db.DbMachineConnect;
import dk.sdu.st4.common.services.IAssembly;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AssemblyRegistry extends DbMachineConnect {

    private static final String BROKER_HOST = "localhost";
    private static final int    BROKER_PORT  = 1883;

    private final Map<String, AssemblyServiceImpl> services  = new ConcurrentHashMap<>();
    private final Queue<String>                    available = new ConcurrentLinkedQueue<>();
    private final Set<String>                      active    = ConcurrentHashMap.newKeySet();

    public AssemblyRegistry() {
        super("ASSEMBLY_STATION");
    }

    // ── DbMachineConnect hooks ───────────────────────────────────────────────

    @Override
    protected void onConnect(String serialNo, String baseUrl) throws Exception {
        AssemblyServiceImpl service = new AssemblyServiceImpl(BROKER_HOST, BROKER_PORT);
        service.connect(serialNo);
        services.put(serialNo, service);
        available.add(serialNo);
    }

    @Override
    protected void onDisconnect(String serialNo) {
        AssemblyServiceImpl service = services.remove(serialNo);
        if (service != null) {
            service.disconnect(serialNo);
        }
        available.remove(serialNo);
        active.remove(serialNo);
    }

    // ── DB bootstrap ─────────────────────────────────────────────────────────

    /** Connects every ASSEMBLY_STATION row found in the machines table. */
    public void loadFromDb() {
        List<String> serialNumbers = DbMachineConnect.getMachinesByType("ASSEMBLY_STATION");
        for (String sn : serialNumbers) {
            try {
                connectMachine(sn);
            } catch (Exception e) {
                System.err.println("[AssemblyRegistry] Failed to connect " + sn + ": " + e.getMessage());
            }
        }
    }

    // ── pool API ──────────────────────────────────────────────────────────────

    /** Returns serial numbers of all connected (idle) assembly stations. */
    public Set<String> getAvailable() {
        return Set.copyOf(available);
    }

    /** Returns live status for every connected Assembly Station: serialNumber, poolStatus, state, healthy, operationId, lastOperationId. */
    public List<Map<String, Object>> getPoolInfo() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, AssemblyServiceImpl> entry : services.entrySet()) {
            String sn = entry.getKey();
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("serialNumber", sn);
            info.put("poolStatus", active.contains(sn) ? "active" : "idle");
            try {
                AssemblyServiceImpl svc = entry.getValue();
                info.put("state",           svc.getStatus());
                info.put("healthy",         svc.getHealth());
                info.put("operationId",     svc.getOperation());
                info.put("lastOperationId", svc.getLastOperationId());
            } catch (Exception e) {
                info.put("state",           -1);
                info.put("healthy",         false);
                info.put("operationId",     -1);
                info.put("lastOperationId", -1);
            }
            result.add(info);
        }
        return result;
    }

    /** Takes an idle assembly station from the pool and marks it active. Returns null if none available. */
    public IAssembly acquire() {
        String sn = available.poll();
        if (sn == null) return null;
        active.add(sn);
        return services.get(sn);
    }

    /** Returns a previously acquired assembly station back to the idle pool. */
    public void release(IAssembly machine) {
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
