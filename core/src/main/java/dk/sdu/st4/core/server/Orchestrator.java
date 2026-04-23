package dk.sdu.st4.core.server;

import dk.sdu.st4.common.data.enums.AgvProgram;
import dk.sdu.st4.common.data.enums.AgvState;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Runs the full production cycle (AGV + warehouse steps) in a background thread.
 * Talks directly to the AGV REST API — no dependency on the agv module.
 */
public class Orchestrator {

    private static final String AGV_URL = "http://localhost:8082/v1/status/";
    private static final int    AGV_POLL_MS = 600;
    private static final int    MAX_EVENTS  = 20;

    private final HttpClient http = HttpClient.newHttpClient();

    // ── volatile state ──────────────────────────────────────────────────────
    private volatile String  lineStatus = "standby";
    private volatile boolean stopRequested  = false;
    private volatile boolean pauseRequested = false;

    // live AGV snapshot (updated by pollUntilIdle and getAgvStatusJson)
    private volatile int    agvBattery    = 0;
    private volatile String agvState      = "Unknown";
    private volatile String agvProgram    = "";
    private volatile String agvTimestamp  = "";

    // ── event log ───────────────────────────────────────────────────────────
    private final LinkedList<Map<String, String>> events = new LinkedList<>();

    // ── public control API ──────────────────────────────────────────────────

    public synchronized void start() {
        if ("running".equals(lineStatus)) return;
        lineStatus   = "running";
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

    /** Returns the latest AGV telemetry as a JSON object string. */
    public String agvJson() {
        refreshAgvStatus();
        return String.format(
            "{\"battery\":%d,\"state\":\"%s\",\"program\":\"%s\",\"timestamp\":\"%s\"}",
            agvBattery, agvState,
            agvProgram.replace("\"", "\\\""),
            agvTimestamp
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
        step(AgvProgram.MoveToStorageOperation,  "AGV · moving to warehouse");
        step(AgvProgram.PickWarehouseOperation,   "AGV · picking tray from warehouse");
        log("ok", "Warehouse · tray picked — item ready");

        step(AgvProgram.MoveToAssemblyOperation, "AGV · moving to assembly station");
        step(AgvProgram.PutAssemblyOperation,     "AGV · placing item at assembly station");

        log("info", "Assembly station · operation started");
        sleep(3000); // placeholder until MQTT assemblystation module is implemented
        if (stopRequested) return;
        log("ok", "Assembly station · cycle accepted");

        step(AgvProgram.PickAssemblyOperation,   "AGV · picking assembled item");
        step(AgvProgram.MoveToStorageOperation,  "AGV · returning to warehouse");
        step(AgvProgram.PutWarehouseOperation,    "AGV · inserting item into warehouse");

        log("ok", "Cycle complete — assembled item stored");
    }

    private void step(AgvProgram program, String description) throws Exception {
        if (stopRequested) return;
        log("info", description);
        agvPut(String.format("{\"Program name\": \"%s\", \"State\": 1}", program.getProgram()));
        agvPut("{\"State\": 2}");
        pollUntilIdle(program.name());
    }

    private void pollUntilIdle(String opName) throws Exception {
        while (!stopRequested) {
            String json   = agvGet();
            int stateCode = extractInt(json, "state");
            updateAgvCache(json);
            AgvState state = AgvState.fromState(stateCode);
            if (state == AgvState.Idle) {
                log("ok", "AGV · " + opName + " completed");
                return;
            }
            sleep(AGV_POLL_MS);
        }
    }

    // ── AGV HTTP helpers ─────────────────────────────────────────────────────

    private String agvGet() throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(AGV_URL)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200)
            throw new Exception("AGV GET returned HTTP " + res.statusCode());
        return res.body();
    }

    private void agvPut(String body) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create(AGV_URL))
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .header("Content-Type", "application/json")
            .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200)
            throw new Exception("AGV PUT returned HTTP " + res.statusCode());
    }

    private void refreshAgvStatus() {
        try { updateAgvCache(agvGet()); } catch (Exception ignored) {}
    }

    private void updateAgvCache(String json) {
        agvBattery   = extractInt(json,    "battery");
        agvState     = AgvState.fromState(extractInt(json, "state")).name();
        agvProgram   = extractString(json, "program name");
        agvTimestamp = extractString(json, "timestamp");
    }

    // ── simple JSON field extraction ─────────────────────────────────────────

    private int extractInt(String json, String key) {
        String search = "\"" + key + "\":";
        int i = json.indexOf(search);
        if (i < 0) return 0;
        i += search.length();
        while (i < json.length() && !Character.isDigit(json.charAt(i))) i++;
        int end = i;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        try { return Integer.parseInt(json.substring(i, end)); } catch (NumberFormatException e) { return 0; }
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int i = json.indexOf(search);
        if (i < 0) return "";
        i += search.length();
        int end = json.indexOf("\"", i);
        return end < 0 ? "" : json.substring(i, end);
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