package dk.sdu.st4.core.server;

import dk.sdu.st4.common.db.DbLineRepository;
import dk.sdu.st4.common.data.AgvStatus;
import dk.sdu.st4.common.data.enums.AgvProgram;
import dk.sdu.st4.common.data.enums.AgvState;
import dk.sdu.st4.common.data.enums.AssemblyState;
import dk.sdu.st4.common.services.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ProductionOrchestrator {

    // ── Step types ── hvert trin svarer til én metode på IAgv, IWarehouse eller IAssembly

    public enum StepType {
        // IAgv: loadProgram(program) + executeProgram() + poll indtil Idle
        AGV_MOVE_TO_CHARGER,
        AGV_MOVE_TO_STORAGE,
        AGV_MOVE_TO_ASSEMBLY,
        AGV_PICK_WAREHOUSE,
        AGV_PUT_WAREHOUSE,
        AGV_PICK_ASSEMBLY,
        AGV_PUT_ASSEMBLY,

        // IWarehouse
        WAREHOUSE_PICK,       // PickItem(trayId, machineId)
        WAREHOUSE_INSERT,     // InsertItem(trayId, itemName, machineId)
        WAREHOUSE_INVENTORY,  // GetInventory(machineId)
        WAREHOUSE_STATE,      // GetState(machineId)

        // IAssembly
        ASSEMBLY_EXECUTE,     // setExecuteOperation() + vent på IDLE
        ASSEMBLY_STATUS,      // getStatus()
        ASSEMBLY_HEALTH       // getHealth()
    }

    // ── Trin med parametre ───────────────────────────────────────────────────
    // variant bruges til warehouse-trin: "parts", "accepted", "defect" osv.

    public static final class StepSpec {
        public final StepType type;
        public final int      trayId;
        public final String   itemName;
        public final String   variant;

        private StepSpec(StepType type, int trayId, String itemName, String variant) {
            this.type     = type;
            this.trayId   = trayId;
            this.itemName = itemName;
            this.variant  = variant;
        }

        public static StepSpec of(StepType type) {
            return new StepSpec(type, 1, "", "");
        }

        public static StepSpec warehousePick(int trayId, String variant) {
            return new StepSpec(StepType.WAREHOUSE_PICK, trayId, "", variant);
        }

        public static StepSpec warehouseInsert(int trayId, String itemName, String variant) {
            return new StepSpec(StepType.WAREHOUSE_INSERT, trayId, itemName, variant);
        }
    }

    // ── Standard-sekvens ────────────────────────────────────────────────────

    public static final List<StepSpec> DEFAULT_SEQUENCE = List.of(
        StepSpec.warehousePick(1, "parts"),
        StepSpec.of(StepType.AGV_MOVE_TO_STORAGE),
        StepSpec.of(StepType.AGV_PICK_WAREHOUSE),
        StepSpec.of(StepType.AGV_MOVE_TO_ASSEMBLY),
        StepSpec.of(StepType.AGV_PUT_ASSEMBLY),
        StepSpec.of(StepType.ASSEMBLY_EXECUTE),
        StepSpec.of(StepType.AGV_PICK_ASSEMBLY),
        StepSpec.of(StepType.AGV_MOVE_TO_STORAGE),
        StepSpec.of(StepType.AGV_PUT_WAREHOUSE),
        StepSpec.warehouseInsert(1, "assembled-part", "accepted")
    );

    // ── Konstanter ───────────────────────────────────────────────────────────

    private static final int AGV_POLL_MS = 600;
    private static final int MAX_EVENTS  = 20;

    // ── Tilstand ─────────────────────────────────────────────────────────────

    private final Optional<IAgvRegistry>       agvRegistry;
    private final Optional<IWarehouseRegistry> warehouseRegistry;
    private final Optional<IAssemblyRegistry>  assemblyRegistry;
    private final String                       lineId;
    private final List<StepSpec>               sequence;

    private volatile String  lineStatus     = "standby";
    private volatile boolean stopRequested  = false;
    private volatile boolean pauseRequested = false;
    private volatile int     cycleCount     = 0;
    private volatile int     failCount      = 0;

    private volatile AgvStatus lastAgvStatus = new AgvStatus(0, "", AgvState.Idle, "");

    private final LinkedList<Map<String, String>> events = new LinkedList<>();

    // ── Constructors ─────────────────────────────────────────────────────────

    public ProductionOrchestrator(Optional<IAgvRegistry> agvRegistry,
                                  Optional<IWarehouseRegistry> warehouseRegistry,
                                  Optional<IAssemblyRegistry> assemblyRegistry,
                                  String lineId) {
        this(agvRegistry, warehouseRegistry, assemblyRegistry, lineId, DEFAULT_SEQUENCE);
    }

    public ProductionOrchestrator(Optional<IAgvRegistry> agvRegistry,
                                  Optional<IWarehouseRegistry> warehouseRegistry,
                                  Optional<IAssemblyRegistry> assemblyRegistry,
                                  String lineId,
                                  List<StepSpec> sequence) {
        this.agvRegistry       = agvRegistry;
        this.warehouseRegistry = warehouseRegistry;
        this.assemblyRegistry  = assemblyRegistry;
        this.lineId            = lineId;
        this.sequence          = List.copyOf(sequence);
    }

    // ── Sekvens-parsing (til DB-templates) ───────────────────────────────────
    // Format: [{"type":"WAREHOUSE_PICK","trayId":1,"variant":"parts"}, ...]

    public static List<StepSpec> parseSequence(String seqJson) {
        List<StepSpec> steps = new ArrayList<>();
        if (seqJson == null || seqJson.isBlank()) return steps;
        seqJson = seqJson.trim();
        if (seqJson.startsWith("[")) seqJson = seqJson.substring(1);
        if (seqJson.endsWith("]"))   seqJson = seqJson.substring(0, seqJson.length() - 1);

        for (String chunk : splitJsonObjects(seqJson)) {
            String type     = extractStr(chunk, "type");
            int    trayId   = extractInt(chunk, "trayId", 1);
            String itemName = extractStr(chunk, "itemName");
            String variant  = extractStr(chunk, "variant");
            try {
                steps.add(new StepSpec(StepType.valueOf(type), trayId, itemName, variant));
            } catch (IllegalArgumentException ignored) {
                System.err.println("[Orchestrator] Ukendt trin-type: " + type);
            }
        }
        return steps;
    }

    // ── Dynamisk maskine-tilslutning ─────────────────────────────────────────

    public void onMachinesAssigned(List<String> serialNos) {
        for (String sn : serialNos) connectMachine(sn);
    }

    public void onMachinesUnassigned(List<String> serialNos) {
        List<Map<String, Object>> allLines = DbLineRepository.getAllLines();
        for (String sn : serialNos) {
            boolean stillUsed = allLines.stream()
                .anyMatch(l -> ((List<String>) l.get("machines")).contains(sn));
            if (!stillUsed) disconnectMachine(sn);
        }
    }

    private void connectMachine(String sn) {
        String type = getMachineType(sn);
        if (type == null) return;
        try {
            switch (type) {
                case "AGV"              -> {
                    if (agvRegistry.isPresent()) agvRegistry.get().connect(sn);
                    else log("warn", "Springer AGV-tilslutning over (" + sn + ") — modul mangler");
                }
                case "WAREHOUSE"        -> {
                    if (warehouseRegistry.isPresent()) warehouseRegistry.get().connect(sn);
                    else log("warn", "Springer Warehouse-tilslutning over (" + sn + ") — modul mangler");
                }
                case "ASSEMBLY_STATION" -> {
                    if (assemblyRegistry.isPresent()) assemblyRegistry.get().connect(sn);
                    else log("warn", "Springer Assembly-tilslutning over (" + sn + ") — modul mangler");
                }
            }
        } catch (Exception e) {
            System.err.println("[Orchestrator] Kunne ikke tilslutte " + sn + ": " + e.getMessage());
        }
    }

    private void disconnectMachine(String sn) {
        String type = getMachineType(sn);
        if (type == null) return;
        switch (type) {
            case "AGV"              -> agvRegistry.ifPresent(r -> r.disconnect(sn));
            case "WAREHOUSE"        -> warehouseRegistry.ifPresent(r -> r.disconnect(sn));
            case "ASSEMBLY_STATION" -> assemblyRegistry.ifPresent(r -> r.disconnect(sn));
        }
    }

    private String getMachineType(String sn) {
        return DbLineRepository.getAllMachines().stream()
            .filter(m -> sn.equals(m.get("serialNo")))
            .map(m -> (String) m.get("type"))
            .findFirst().orElse(null);
    }

    // ── Kontrol-API ──────────────────────────────────────────────────────────

    public synchronized void start() {
        if ("running".equals(lineStatus)) return;

        List<String> missing = new ArrayList<>();
        if (agvRegistry.isEmpty())       missing.add("AGV");
        if (warehouseRegistry.isEmpty()) missing.add("Warehouse");
        if (assemblyRegistry.isEmpty())  missing.add("Assembly");
        if (!missing.isEmpty()) {
            log("err", "Kan ikke starte cyklus — manglende moduler i mods-mvn: " + missing);
            return;
        }

        lineStatus     = "running";
        stopRequested  = false;
        pauseRequested = false;
        Thread t = new Thread(this::cycleLoop, "orchestrator");
        t.setDaemon(true);
        t.start();
        log("ok", "Linje startet — " + sequence.size() + " trin i sekvens");
    }

    public boolean isReady() {
        return agvRegistry.isPresent()
            && warehouseRegistry.isPresent()
            && assemblyRegistry.isPresent();
    }

    public List<String> missingModules() {
        List<String> missing = new ArrayList<>();
        if (agvRegistry.isEmpty())       missing.add("AGV");
        if (warehouseRegistry.isEmpty()) missing.add("Warehouse");
        if (assemblyRegistry.isEmpty())  missing.add("Assembly");
        return missing;
    }

    public synchronized void pause() {
        if (!"running".equals(lineStatus)) return;
        lineStatus     = "paused";
        pauseRequested = true;
        log("info", "Linje pauset");
    }

    public synchronized void stop() {
        lineStatus    = "stopped";
        stopRequested = true;
        log("warn", "Linje stoppet");
    }

    public synchronized void abort() {
        lineStatus    = "alarm";
        stopRequested = true;
        log("err", "ABORT — nødstop aktiveret");
    }

    // ── Status-accessorer ────────────────────────────────────────────────────

    public String getLineStatus() { return lineStatus; }
    public int    getCycleCount() { return cycleCount; }
    public int    getFailCount()  { return failCount;  }

    public String agvJson() {
        AgvStatus s = lastAgvStatus;
        String stateName = s.getState() != null ? s.getState().name() : "Unknown";
        String program   = s.getProgramName() != null ? s.getProgramName() : "";
        String timestamp = s.getTimestamp()   != null ? s.getTimestamp()   : "";
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
        StringBuilder agvArr = new StringBuilder("[");
        StringBuilder whArr  = new StringBuilder("[");
        StringBuilder asArr  = new StringBuilder("[");
        boolean firstAgv = true, firstWh = true, firstAs = true;

        if (agvRegistry.isPresent()) {
            for (Map<String, Object> m : agvRegistry.get().getMachinesStatus()) {
                String battery = m.get("battery") != null ? m.get("battery").toString() : "null";
                String entry = String.format(
                    "{\"serialNo\":\"%s\",\"poolStatus\":\"%s\",\"agvState\":\"%s\",\"battery\":%s,\"program\":\"%s\"}",
                    esc(str(m, "serialNo")), str(m, "poolStatus"), str(m, "agvState"),
                    battery, esc(str(m, "program")));
                if (!firstAgv) agvArr.append(",");
                agvArr.append(entry);
                firstAgv = false;
            }
        }

        if (warehouseRegistry.isPresent()) {
            for (Map<String, Object> m : warehouseRegistry.get().getMachinesStatus()) {
                int state = m.get("warehouseState") instanceof Number n ? n.intValue() : 0;
                String entry = String.format(
                    "{\"serialNo\":\"%s\",\"poolStatus\":\"%s\",\"warehouseState\":%d}",
                    esc(str(m, "serialNo")), str(m, "poolStatus"), state);
                if (!firstWh) whArr.append(",");
                whArr.append(entry);
                firstWh = false;
            }
        }

        if (assemblyRegistry.isPresent()) {
            for (Map<String, Object> m : assemblyRegistry.get().getMachinesStatus()) {
                int     state     = m.get("state")           instanceof Number n ? n.intValue()  : 0;
                String  healthy   = m.get("healthy")         instanceof Boolean b ? b.toString() : "null";
                int     opId      = m.get("operationId")     instanceof Number n ? n.intValue()  : -1;
                int     lastOpId  = m.get("lastOperationId") instanceof Number n ? n.intValue()  : -1;
                String entry = String.format(
                    "{\"serialNo\":\"%s\",\"poolStatus\":\"%s\",\"state\":%d,\"healthy\":%s,\"operationId\":%d,\"lastOperationId\":%d}",
                    esc(str(m, "serialNo")), str(m, "poolStatus"), state, healthy, opId, lastOpId);
                if (!firstAs) asArr.append(",");
                asArr.append(entry);
                firstAs = false;
            }
        }

        return String.format("{\"agv\":%s,\"warehouse\":%s,\"assembly\":%s}",
            agvArr.append("]"), whArr.append("]"), asArr.append("]"));
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    // ── Cyklus-løkke ─────────────────────────────────────────────────────────

    private void cycleLoop() {
        while (!stopRequested) {
            if (pauseRequested) { sleep(200); continue; }
            try {
                runOneCycle();
            } catch (Exception e) {
                log("err", "Cyklusfejl: " + e.getMessage());
                failCount++;
                if (lineId != null) DbLineRepository.recordCycleFailure(lineId);
                lineStatus    = "alarm";
                stopRequested = true;
            }
        }
    }

    private void runOneCycle() throws Exception {
        // start() har allerede verificeret at alle tre registries er til stede
        IAgvRegistry      agvReg = agvRegistry.get();
        IAssemblyRegistry asReg  = assemblyRegistry.get();

        IAgv      agv      = agvReg.acquire();
        IAssembly assembly = asReg.acquire();

        if (agv == null || assembly == null) {
            if (agv      != null) agvReg.release(agv);
            if (assembly != null) asReg.release(assembly);
            throw new Exception("Ingen maskiner tilgængelige");
        }

        try {
            for (StepSpec step : sequence) {
                if (stopRequested) return;
                executeStep(step, agv, assembly);
            }
            cycleCount++;
            if (lineId != null) DbLineRepository.recordCycleComplete(lineId);
            log("ok", "Cyklus " + cycleCount + " fuldført");
        } finally {
            agvReg.release(agv);
            asReg.release(assembly);
        }
    }

    // ── Trin-dispatcher ──────────────────────────────────────────────────────

    private void executeStep(StepSpec step, IAgv agv, IAssembly assembly) throws Exception {
        switch (step.type) {

            // AGV — loadProgram + executeProgram + poll indtil Idle
            case AGV_MOVE_TO_CHARGER  -> agvStep(agv, AgvProgram.MoveToChargerOperation);
            case AGV_MOVE_TO_STORAGE  -> agvStep(agv, AgvProgram.MoveToStorageOperation);
            case AGV_MOVE_TO_ASSEMBLY -> agvStep(agv, AgvProgram.MoveToAssemblyOperation);
            case AGV_PICK_WAREHOUSE   -> agvStep(agv, AgvProgram.PickWarehouseOperation);
            case AGV_PUT_WAREHOUSE    -> agvStep(agv, AgvProgram.PutWarehouseOperation);
            case AGV_PICK_ASSEMBLY    -> agvStep(agv, AgvProgram.PickAssemblyOperation);
            case AGV_PUT_ASSEMBLY     -> agvStep(agv, AgvProgram.PutAssemblyOperation);

            // Warehouse — vælges pr. variant (fx "parts", "accepted", "defect")
            case WAREHOUSE_PICK -> {
                IWarehouse wh = resolveWarehouse(step.variant);
                log("info", "Warehouse[" + step.variant + "] · henter bakke " + step.trayId);
                wh.PickItem(step.trayId, "");
                log("ok", "Warehouse · bakke klar ved udgang");
            }
            case WAREHOUSE_INSERT -> {
                IWarehouse wh = resolveWarehouse(step.variant);
                log("info", "Warehouse[" + step.variant + "] · indsætter bakke " + step.trayId);
                wh.InsertItem(step.trayId, step.itemName, "");
                log("ok", "Warehouse · item gemt");
            }
            case WAREHOUSE_INVENTORY -> {
                IWarehouse wh = resolveWarehouse(step.variant);
                log("info", "Warehouse[" + step.variant + "] · henter lager");
                wh.GetInventory("");
            }
            case WAREHOUSE_STATE -> {
                IWarehouse wh = resolveWarehouse(step.variant);
                int state = wh.GetState("");
                log("info", "Warehouse[" + step.variant + "] · tilstand = " + state);
            }

            // Assembly
            case ASSEMBLY_EXECUTE -> {
                log("info", "Assembly · starter operation");
                assembly.setExecuteOperation();
                waitForAssemblyIdle(assembly);
                log("ok", "Assembly · operation fuldført");
            }
            case ASSEMBLY_STATUS -> {
                int status = assembly.getStatus();
                log("info", "Assembly · status = " + status);
            }
            case ASSEMBLY_HEALTH -> {
                boolean healthy = assembly.getHealth();
                log("info", "Assembly · sund = " + healthy);
            }
        }
    }

    // ── AGV-hjælpere ─────────────────────────────────────────────────────────

    private void agvStep(IAgv agv, AgvProgram program) throws Exception {
        if (stopRequested) return;
        log("info", "AGV · " + program.name());
        agv.loadProgram(program);
        agv.executeProgram();
        pollUntilAgvIdle(agv, program.name());
    }

    private void pollUntilAgvIdle(IAgv agv, String opName) throws Exception {
        while (!stopRequested) {
            AgvStatus status = agv.getStatus();
            lastAgvStatus = status;
            if (status.getState() == AgvState.Idle) {
                log("ok", "AGV · " + opName + " færdig");
                return;
            }
            sleep(AGV_POLL_MS);
        }
    }

    // ── Assembly-hjælpere ────────────────────────────────────────────────────

    private void waitForAssemblyIdle(IAssembly assembly) throws Exception {
        int prevLastOp = assembly.getLastOperationId();

        long startDeadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < startDeadline) {
            if (stopRequested) return;
            int state    = assembly.getStatus();
            int lastOpId = assembly.getLastOperationId();
            if (state == AssemblyState.ERROR.getCode())
                throw new Exception("Assembly rapporterede ERROR");
            if (lastOpId != prevLastOp && state == AssemblyState.IDLE.getCode()) return;
            if (state == AssemblyState.EXECUTING.getCode()) break;
            sleep(300);
        }

        long runDeadline = System.currentTimeMillis() + 300_000;
        while (System.currentTimeMillis() < runDeadline) {
            if (stopRequested) return;
            int state = assembly.getStatus();
            if (state == AssemblyState.IDLE.getCode())  return;
            if (state == AssemblyState.ERROR.getCode())
                throw new Exception("Assembly rapporterede ERROR");
            sleep(300);
        }

        throw new Exception("Assembly timeout (state=" + assembly.getStatus()
                + ", lastOp=" + assembly.getLastOperationId() + ")");
    }

    // ── Warehouse-opslag ─────────────────────────────────────────────────────

    private IWarehouse resolveWarehouse(String variant) throws Exception {
        IWarehouseRegistry wr = warehouseRegistry.orElseThrow(
            () -> new Exception("Warehouse-modul mangler i mods-mvn"));
        IWarehouse wh = (variant == null || variant.isBlank())
            ? wr.getWarehouse("")
            : wr.getWarehouseByVariant(variant);
        if (wh == null) throw new Exception("Intet warehouse fundet for variant: '" + variant + "'");
        return wh;
    }

    // ── JSON-sekvens-parser ──────────────────────────────────────────────────

    private static List<String> splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}' && --depth == 0 && start >= 0) {
                objects.add(json.substring(start, i + 1));
                start = -1;
            }
        }
        return objects;
    }

    private static String extractStr(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int s = json.indexOf(marker);
        if (s < 0) return "";
        s += marker.length();
        int e = json.indexOf("\"", s);
        return e < 0 ? "" : json.substring(s, e);
    }

    private static int extractInt(String json, String key, int def) {
        String marker = "\"" + key + "\":";
        int s = json.indexOf(marker);
        if (s < 0) return def;
        s += marker.length();
        while (s < json.length() && json.charAt(s) == ' ') s++;
        int e = s;
        while (e < json.length() && Character.isDigit(json.charAt(e))) e++;
        if (s == e) return def;
        try { return Integer.parseInt(json.substring(s, e)); } catch (NumberFormatException ex) { return def; }
    }

    // ── Log og utility ───────────────────────────────────────────────────────

    private synchronized void log(String lvl, String msg) {
        String t = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        events.addFirst(Map.of("t", t, "lvl", lvl, "m", msg));
        while (events.size() > MAX_EVENTS) events.removeLast();
        System.out.printf("[%s] [%-4s] %s%n", t, lvl.toUpperCase(), msg);
    }

    private static String esc(String s) { return s != null ? s.replace("\"", "\\\"") : ""; }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
