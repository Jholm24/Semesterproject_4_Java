package dk.sdu.st4.app;

import dk.sdu.st4.app.Registries.AgvRegistry;
import dk.sdu.st4.app.Registries.AssemblyRegistry;
import dk.sdu.st4.app.Registries.WarehouseRegistry;
import dk.sdu.st4.common.data.AgvStatus;
import dk.sdu.st4.common.data.enums.AgvProgram;
import dk.sdu.st4.common.data.enums.AgvState;
import dk.sdu.st4.common.services.IAgv;
import dk.sdu.st4.common.services.IWarehouse;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Drives the full production cycle by acquiring one machine of each type
 * from the registries, running the cycle, then releasing them back.
 *
 * Intended cycle:
 *  1. Warehouse.PickItem       — move tray to outlet
 *  2. AGV: move → pick → move → put  (warehouse → assembly)
 *  3. Assembly.setExecuteOperation   — await MQTT status callback
 *  4. AGV: pick → move → put        (assembly → warehouse)
 *  5. Warehouse.InsertItem     — store assembled item
 */
public class ProductionOrchestrator {

    private static final int AGV_POLL_MS = 600;
    private static final int MAX_EVENTS  = 20;

    private final AgvRegistry      agvRegistry;
    private final WarehouseRegistry warehouseRegistry;
    private final AssemblyRegistry  assemblyRegistry;

    // ── volatile state ──────────────────────────────────────────────────────
    private volatile String  lineStatus     = "standby";
    private volatile boolean stopRequested  = false;
    private volatile boolean pauseRequested = false;

    // cached AGV snapshot, updated each poll tick
    private volatile AgvStatus lastAgvStatus = new AgvStatus(0, "", AgvState.Idle, "");

    // ── event log ───────────────────────────────────────────────────────────
    private final LinkedList<Map<String, String>> events = new LinkedList<>();

    public ProductionOrchestrator(AgvRegistry agvRegistry,
                                  WarehouseRegistry warehouseRegistry,
                                  AssemblyRegistry assemblyRegistry) {
        this.agvRegistry       = agvRegistry;
        this.warehouseRegistry = warehouseRegistry;
        this.assemblyRegistry  = assemblyRegistry;
    }

    // ── public control API ──────────────────────────────────────────────────

    public synchronized void start() {
        if ("running".equals(lineStatus)) return;
        lineStatus     = "running";
        stopRequested  = false;
        pauseRequested = false;
        Thread t = new Thread(this::cycleLoop, "orchestrator");
        t.setDaemon(true);
        t.start();
        log("ok", "Line started — production queue initiated");
    }

    public synchronized void pause() {
        if (!"running".equals(lineStatus)) return;
        lineStatus     = "paused";
        pauseRequested = true;
        log("info", "Line paused");
    }

    public synchronized void stop() {
        lineStatus    = "stopped";
        stopRequested = true;
        log("warn", "Line stopped — parked in safe state");
    }

    public synchronized void abort() {
        lineStatus    = "alarm";
        stopRequested = true;
        log("err", "ABORT — emergency stop engaged");
    }

    // ── status accessors ────────────────────────────────────────────────────

    public String getLineStatus() { return lineStatus; }

    public String agvJson() {
        AgvStatus s = lastAgvStatus;
        String stateName  = s.getState() != null ? s.getState().name() : "Unknown";
        String program    = s.getProgramName() != null ? s.getProgramName() : "";
        String timestamp  = s.getTimestamp()   != null ? s.getTimestamp()   : "";
        return String.format(
            "{\"battery\":%d,\"state\":\"%s\",\"program\":\"%s\",\"timestamp\":\"%s\"}",
            s.getBattery(), stateName,
            program.replace("\"", "\\\""),
            timestamp
        );
    }

    public synchronized List<Map<String, String>> getEvents() {
        return new ArrayList<>(events);
    }

    // ── production cycle ────────────────────────────────────────────────────

    private void cycleLoop() {
        while (!stopRequested) {
            if (pauseRequested) {
                sleep(200);
                continue;
            }
            try {
                runOneCycle();
            } catch (Exception e) {
                log("err", "Cycle error: " + e.getMessage());
                lineStatus    = "alarm";
                stopRequested = true;
            }
        }
    }

    private void runOneCycle() throws Exception {
        IAgv agv           = agvRegistry.acquire();
        IWarehouse warehouse = warehouseRegistry.acquire();

        if (agv == null || warehouse == null) {
            if (agv != null)       agvRegistry.release(agv);
            if (warehouse != null) warehouseRegistry.release(warehouse);
            throw new Exception("No machines available to run a cycle");
        }

        try {
            step(agv, AgvProgram.MoveToStorageOperation,  "AGV · moving to warehouse");
            step(agv, AgvProgram.PickWarehouseOperation,   "AGV · picking tray from warehouse");
            log("ok", "Warehouse · tray picked — item ready");

            step(agv, AgvProgram.MoveToAssemblyOperation, "AGV · moving to assembly station");
            step(agv, AgvProgram.PutAssemblyOperation,     "AGV · placing item at assembly station");

            log("info", "Assembly station · operation started");
            sleep(3000); // placeholder until MQTT assemblystation module is implemented
            if (stopRequested) return;
            log("ok", "Assembly station · cycle accepted");

            step(agv, AgvProgram.PickAssemblyOperation,   "AGV · picking assembled item");
            step(agv, AgvProgram.MoveToStorageOperation,  "AGV · returning to warehouse");
            step(agv, AgvProgram.PutWarehouseOperation,    "AGV · inserting item into warehouse");

            log("ok", "Cycle complete — assembled item stored");
        } finally {
            agvRegistry.release(agv);
            warehouseRegistry.release(warehouse);
        }
    }

    private void step(IAgv agv, AgvProgram program, String description) throws Exception {
        if (stopRequested) return;
        log("info", description);
        agv.loadProgram(program);
        agv.executeProgram();
        pollUntilIdle(agv, program.name());
    }

    private void pollUntilIdle(IAgv agv, String opName) throws Exception {
        while (!stopRequested) {
            AgvStatus status = agv.getStatus();
            lastAgvStatus = status;
            if (status.getState() == AgvState.Idle) {
                log("ok", "AGV · " + opName + " completed");
                return;
            }
            sleep(AGV_POLL_MS);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private synchronized void log(String lvl, String msg) {
        String t = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        events.addFirst(Map.of("t", t, "lvl", lvl, "m", msg));
        while (events.size() > MAX_EVENTS) events.removeLast();
        System.out.printf("[%s] [%-4s] %s%n", t, lvl.toUpperCase(), msg);
    }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}