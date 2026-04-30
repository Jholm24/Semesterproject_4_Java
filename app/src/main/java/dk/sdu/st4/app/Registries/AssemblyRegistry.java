package dk.sdu.st4.app.Registries;

import dk.sdu.st4.assemblystation.AssemblyServiceImpl;
import dk.sdu.st4.common.db.DbMachineConnect;
import dk.sdu.st4.common.services.IAssembly;

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
            connectMachine(sn);
        }
    }

    // ── pool API ──────────────────────────────────────────────────────────────

    /** Returns serial numbers of all connected (idle) assembly stations. */
    public Set<String> getAvailable() {
        return Set.copyOf(available);
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
