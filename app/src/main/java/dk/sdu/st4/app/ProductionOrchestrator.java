package dk.sdu.st4.app;

import dk.sdu.st4.app.Registries.AgvRegistry;
import dk.sdu.st4.app.Registries.AssemblyRegistry;
import dk.sdu.st4.app.Registries.WarehouseRegistry;
import dk.sdu.st4.common.data.AgvStatus;
import dk.sdu.st4.common.data.enums.AgvProgram;
import dk.sdu.st4.common.data.enums.AgvState;
import dk.sdu.st4.common.services.IAgv;
import dk.sdu.st4.common.services.IAssembly;
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
    private static final int TRAY_ID     = 1;

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

    public String machinesJson() {
        StringBuilder sb = new StringBuilder("{");

        sb.append("\"agv\":[");
        List<Map<String, Object>> agvList = agvRegistry.getPoolInfo();
        for (int i = 0; i < agvList.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, Object> m = agvList.get(i);
            Object battery = m.get("battery");
            Object state   = m.get("agvState");
            Object program = m.get("program");
            sb.append("{")
              .append("\"serialNumber\":\"").append(escapeJson((String) m.get("serialNumber"))).append("\",")
              .append("\"poolStatus\":\"").append(m.get("poolStatus")).append("\",")
              .append("\"battery\":").append(battery != null ? battery : "null").append(",")
              .append("\"agvState\":").append(state   != null ? "\"" + state   + "\"" : "null").append(",")
              .append("\"program\":") .append(program != null ? "\"" + escapeJson((String) program) + "\"" : "null")
              .append("}");
        }
        sb.append("],");

        sb.append("\"warehouse\":[");
        List<Map<String, Object>> whList = warehouseRegistry.getPoolInfo();
        for (int i = 0; i < whList.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, Object> m = whList.get(i);
            sb.append("{")
              .append("\"serialNumber\":\"").append(escapeJson((String) m.get("serialNumber"))).append("\",")
              .append("\"poolStatus\":\"").append(m.get("poolStatus")).append("\",")
              .append("\"warehouseState\":").append(m.get("warehouseState"))
              .append("}");
        }
        sb.append("],");

        sb.append("\"assembly\":[");
        List<Map<String, Object>> asList = assemblyRegistry.getPoolInfo();
        for (int i = 0; i < asList.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, Object> m = asList.get(i);
            sb.append("{")
              .append("\"serialNumber\":\"").append(escapeJson((String) m.get("serialNumber"))).append("\",")
              .append("\"poolStatus\":\"").append(m.get("poolStatus")).append("\",")
              .append("\"state\":").append(m.get("state")).append(",")
              .append("\"healthy\":").append(m.get("healthy")).append(",")
              .append("\"operationId\":").append(m.get("operationId")).append(",")
              .append("\"lastOperationId\":").append(m.get("lastOperationId"))
              .append("}");
        }
        sb.append("]}");

        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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
        IAgv       agv       = agvRegistry.acquire();
        IWarehouse warehouse = warehouseRegistry.acquire();
        IAssembly  assembly  = assemblyRegistry.acquire();

        if (agv == null || warehouse == null || assembly == null) {
            if (agv       != null) agvRegistry.release(agv);
            if (warehouse != null) warehouseRegistry.release(warehouse);
            if (assembly  != null) assemblyRegistry.release(assembly);
            throw new Exception("No machines available to run a cycle");
        }

        try {
            // 1. Warehouse picks tray to outlet
            log("info", "Warehouse · picking tray " + TRAY_ID);
            warehouse.PickItem(TRAY_ID, "");
            log("ok", "Warehouse · tray ready at outlet");

            // 2. AGV: warehouse → assembly
            step(agv, AgvProgram.MoveToStorageOperation,  "AGV · moving to warehouse");
            step(agv, AgvProgram.PickWarehouseOperation,  "AGV · picking tray from warehouse");
            step(agv, AgvProgram.MoveToAssemblyOperation, "AGV · moving to assembly station");
            step(agv, AgvProgram.PutAssemblyOperation,    "AGV · placing item at assembly station");

            // 3. Assembly operation (MQTT)
            log("info", "Assembly station · starting operation");
            assembly.setExecuteOperation();
            waitForAssemblyComplete(assembly);
            log("ok", "Assembly station · operation complete");

            // 4. AGV: assembly → warehouse
            step(agv, AgvProgram.PickAssemblyOperation,  "AGV · picking assembled item");
            step(agv, AgvProgram.MoveToStorageOperation, "AGV · returning to warehouse");
            step(agv, AgvProgram.PutWarehouseOperation,  "AGV · inserting item into warehouse");

            // 5. Warehouse stores assembled item
            log("info", "Warehouse · storing assembled item");
            warehouse.InsertItem(TRAY_ID, "assembled-part", "");
            log("ok", "Cycle complete — assembled item stored");

        } finally {
            agvRegistry.release(agv);
            warehouseRegistry.release(warehouse);
            assemblyRegistry.release(assembly);
        }
    }

    private void waitForAssemblyComplete(IAssembly assembly) throws Exception {
        int prevLastOp = assembly.getLastOperationId();

        // Phase 1: wait up to 30 s for EXECUTING (state=1) to appear
        long startDeadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < startDeadline) {
            if (stopRequested) return;
            int state    = assembly.getStatus();
            int lastOpId = assembly.getLastOperationId();
            if (state == 2) throw new Exception("Assembly station reported ERROR");
            if (lastOpId != prevLastOp && state == 0) return; // fast-complete before we polled
            if (state == 1) break;
            sleep(300);
        }

        // Phase 2: wait up to 5 minutes for IDLE (state=0) — assembly operations can be slow
        long completeDeadline = System.currentTimeMillis() + 300_000;
        while (System.currentTimeMillis() < completeDeadline) {
            if (stopRequested) return;
            int state = assembly.getStatus();
            if (state == 0) return;
            if (state == 2) throw new Exception("Assembly station reported ERROR");
            sleep(300);
        }

        throw new Exception("Assembly station timed out (state=" + assembly.getStatus()
                + ", lastOp=" + assembly.getLastOperationId() + ")");
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