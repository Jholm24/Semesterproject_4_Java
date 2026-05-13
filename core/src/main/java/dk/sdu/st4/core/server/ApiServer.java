package dk.sdu.st4.core.server;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dk.sdu.st4.common.db.DbLineRepository;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server (JDK HttpServer) that:
 *  - serves the React UI from core/ui/ as static files
 *  - exposes /api/status, /api/events, /api/control for the UI to call
 */
public class ApiServer {

    private final ProductionOrchestrator orchestrator;
    private final int          port;
    private final Path         uiRoot;
    private HttpServer         server;

    public ApiServer(ProductionOrchestrator orchestrator, int port, Path uiRoot) {
        this.orchestrator = orchestrator;
        this.port   = port;
        this.uiRoot = uiRoot;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/status",    ex -> handle(ex, this::handleStatus));
        server.createContext("/api/events",    ex -> handle(ex, this::handleEvents));
        server.createContext("/api/control",   ex -> handle(ex, this::handleControl));
        server.createContext("/api/machines",  ex -> handle(ex, this::handleMachines));
        server.createContext("/api/catalog",   ex -> handle(ex, this::handleCatalog));
        server.createContext("/api/lines",     ex -> handle(ex, this::handleLines));
        server.createContext("/api/employees", ex -> handle(ex, this::handleEmployees));
        server.createContext("/api/templates", ex -> handle(ex, this::handleTemplates));
        server.createContext("/",              ex -> handle(ex, this::handleStatic));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    // ── route handlers ───────────────────────────────────────────────────────

    private void handleStatus(HttpExchange ex) throws IOException {
        if (isPreflight(ex)) return;
        if (!"GET".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(405, -1); return; }

        String body = String.format(
            "{\"agv\":%s,\"lineStatus\":\"%s\",\"cycles\":%d,\"fails\":%d}",
            orchestrator.agvJson(), orchestrator.getLineStatus(),
            orchestrator.getCycleCount(), orchestrator.getFailCount()
        );
        sendJson(ex, 200, body);
    }

    private void handleEvents(HttpExchange ex) throws IOException {
        if (isPreflight(ex)) return;
        if (!"GET".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(405, -1); return; }

        List<Map<String, String>> events = orchestrator.getEvents();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < events.size(); i++) {
            Map<String, String> e = events.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"t\":\"%s\",\"lvl\":\"%s\",\"m\":\"%s\"}",
                e.get("t"), e.get("lvl"),
                e.get("m").replace("\\", "\\\\").replace("\"", "\\\"")
            ));
        }
        sb.append("]");
        sendJson(ex, 200, sb.toString());
    }

    private void handleControl(HttpExchange ex) throws IOException {
        if (isPreflight(ex)) return;
        if (!"POST".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(405, -1); return; }

        String body   = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        String action = extractJsonString(body, "action");

        switch (action) {
            case "start" -> orchestrator.start();
            case "pause" -> orchestrator.pause();
            case "stop"  -> orchestrator.stop();
            case "abort" -> orchestrator.abort();
        }

        sendJson(ex, 200,
            "{\"ok\":true,\"lineStatus\":\"" + orchestrator.getLineStatus() + "\"}");
    }

    private void handleMachines(HttpExchange ex) throws IOException {
        if (isPreflight(ex)) return;
        if (!"GET".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(405, -1); return; }
        sendJson(ex, 200, orchestrator.machinesJson());
    }

    private void handleCatalog(HttpExchange ex) throws IOException {
        if (isPreflight(ex)) return;
        if (!"GET".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(405, -1); return; }

        List<Map<String, Object>> all = DbLineRepository.getAllMachines();
        StringBuilder agv = new StringBuilder();
        StringBuilder wh  = new StringBuilder();
        StringBuilder asm = new StringBuilder();

        for (Map<String, Object> m : all) {
            String type = (String) m.get("type");
            String sn   = (String) m.get("serialNo");
            String entry = "{\"serialNo\":\"" + escapeJson(sn) + "\"}";
            switch (type) {
                case "AGV"              -> { if (agv.length() > 0) agv.append(","); agv.append(entry); }
                case "WAREHOUSE"        -> { if (wh.length()  > 0) wh.append(",");  wh.append(entry); }
                case "ASSEMBLY_STATION" -> { if (asm.length() > 0) asm.append(","); asm.append(entry); }
            }
        }

        sendJson(ex, 200, "{\"agv\":[" + agv + "],\"warehouse\":[" + wh + "],\"assembly\":[" + asm + "]}");
    }

    // ── /api/lines ───────────────────────────────────────────────────────────

    private void handleLines(HttpExchange ex) throws IOException {
        if (isPreflight(ex)) return;
        String method = ex.getRequestMethod();
        String body   = "GET".equals(method) ? "" :
                        new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        switch (method) {
            case "GET" -> sendJson(ex, 200, buildLinesJson());
            case "POST" -> {
                String id      = extractJsonString(body, "id");
                String title   = extractJsonString(body, "title");
                String product = extractJsonString(body, "product");
                List<String> machines  = extractJsonArray(body, "machines");
                List<String> operators = extractJsonArray(body, "operators");
                DbLineRepository.createLine(id, title.isEmpty() ? id : title, product, machines, operators);
                orchestrator.onMachinesAssigned(machines);
                sendJson(ex, 201, buildLinesJson());
            }
            case "PUT" -> {
                String id      = extractJsonString(body, "id");
                String title   = extractJsonString(body, "title");
                String product = extractJsonString(body, "product");
                List<String> machines  = extractJsonArray(body, "machines");
                List<String> operators = extractJsonArray(body, "operators");
                List<String> oldMachines = DbLineRepository.getMachinesForLine(id);
                DbLineRepository.updateLine(id, title, product, machines, operators);
                List<String> added = new ArrayList<>(machines); added.removeAll(oldMachines);
                List<String> removed = new ArrayList<>(oldMachines); removed.removeAll(machines);
                orchestrator.onMachinesAssigned(added);
                orchestrator.onMachinesUnassigned(removed);
                sendJson(ex, 200, buildLinesJson());
            }
            case "PATCH" -> {
                String id      = extractJsonString(body, "id");
                String status  = extractJsonString(body, "status");
                int    cycles  = extractJsonInt(body, "cycles");
                double success = extractJsonDouble(body, "success");
                int    warnings = extractJsonInt(body, "warnings");
                DbLineRepository.updateLineStatus(id, status, cycles, success, warnings);
                sendJson(ex, 200, "{\"ok\":true}");
            }
            case "DELETE" -> {
                String id = extractJsonString(body, "id");
                List<String> machines = DbLineRepository.getMachinesForLine(id);
                DbLineRepository.deleteLine(id);
                orchestrator.onMachinesUnassigned(machines);
                sendJson(ex, 200, buildLinesJson());
            }
            default -> ex.sendResponseHeaders(405, -1);
        }
    }

    private String buildLinesJson() {
        List<Map<String, Object>> lines = DbLineRepository.getAllLines();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, Object> L = lines.get(i);
            sb.append("{");
            sb.append("\"id\":\"").append(escapeJson((String) L.get("id"))).append("\",");
            sb.append("\"name\":\"").append(escapeJson((String) L.get("name"))).append("\",");
            sb.append("\"product\":\"").append(escapeJson((String) L.get("product"))).append("\",");
            sb.append("\"status\":\"").append(L.get("status")).append("\",");
            sb.append("\"cycles\":").append(L.get("cycles")).append(",");
            sb.append("\"success\":").append(L.get("success")).append(",");
            sb.append("\"warnings\":").append(L.get("warnings")).append(",");
            sb.append("\"machines\":").append(toJsonStringArray((List<String>) L.get("machines"))).append(",");
            sb.append("\"operators\":").append(toJsonStringArray((List<String>) L.get("operators")));
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    // ── /api/employees ───────────────────────────────────────────────────────

    private void handleEmployees(HttpExchange ex) throws IOException {
        if (isPreflight(ex)) return;
        String method = ex.getRequestMethod();
        String body   = "GET".equals(method) ? "" :
                        new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        switch (method) {
            case "GET" -> sendJson(ex, 200, buildEmployeesJson());
            case "POST" -> {
                DbLineRepository.createEmployee(
                    extractJsonString(body, "id"),
                    extractJsonString(body, "name"),
                    extractJsonString(body, "username"),
                    extractJsonString(body, "role"),
                    extractJsonString(body, "pic"),
                    extractJsonString(body, "since"),
                    extractJsonString(body, "password")
                );
                sendJson(ex, 201, buildEmployeesJson());
            }
            case "PUT" -> {
                DbLineRepository.updateEmployee(
                    extractJsonString(body, "id"),
                    extractJsonString(body, "name"),
                    extractJsonString(body, "username"),
                    extractJsonString(body, "role"),
                    extractJsonString(body, "pic"),
                    extractJsonString(body, "password")
                );
                sendJson(ex, 200, buildEmployeesJson());
            }
            case "DELETE" -> {
                DbLineRepository.deleteEmployee(extractJsonString(body, "id"));
                sendJson(ex, 200, buildEmployeesJson());
            }
            default -> ex.sendResponseHeaders(405, -1);
        }
    }

    private String buildEmployeesJson() {
        List<Map<String, Object>> emps = DbLineRepository.getAllEmployees();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < emps.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, Object> e = emps.get(i);
            sb.append("{");
            sb.append("\"id\":\"").append(escapeJson((String) e.get("id"))).append("\",");
            sb.append("\"name\":\"").append(escapeJson((String) e.get("name"))).append("\",");
            sb.append("\"username\":\"").append(escapeJson(nullStr(e.get("username")))).append("\",");
            sb.append("\"role\":\"").append(escapeJson((String) e.get("role"))).append("\",");
            sb.append("\"pic\":\"").append(escapeJson(nullStr(e.get("pic")))).append("\",");
            sb.append("\"since\":\"").append(escapeJson(nullStr(e.get("since")))).append("\"");
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    // ── /api/templates ───────────────────────────────────────────────────────

    private void handleTemplates(HttpExchange ex) throws IOException {
        if (isPreflight(ex)) return;
        String method = ex.getRequestMethod();
        String query  = ex.getRequestURI().getRawQuery(); // e.g. "lineId=line-1"
        String body   = "GET".equals(method) ? "" :
                        new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        switch (method) {
            case "GET" -> {
                String lineId = extractQueryParam(query, "lineId");
                sendJson(ex, 200, buildTemplatesJson(lineId));
            }
            case "POST" -> {
                String lineId  = extractJsonString(body, "lineId");
                String name    = extractJsonString(body, "name");
                String seqJson = extractJsonString(body, "seq");
                int newId = DbLineRepository.createTemplate(lineId, name, seqJson);
                sendJson(ex, 201, "{\"id\":" + newId + "}");
            }
            case "DELETE" -> {
                int id = extractJsonInt(body, "id");
                DbLineRepository.deleteTemplate(id);
                sendJson(ex, 200, "{\"ok\":true}");
            }
            default -> ex.sendResponseHeaders(405, -1);
        }
    }

    private String buildTemplatesJson(String lineId) {
        List<Map<String, Object>> templates = DbLineRepository.getTemplatesByLine(lineId);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < templates.size(); i++) {
            if (i > 0) sb.append(",");
            Map<String, Object> t = templates.get(i);
            sb.append("{");
            sb.append("\"id\":").append(t.get("id")).append(",");
            sb.append("\"lineId\":\"").append(escapeJson((String) t.get("lineId"))).append("\",");
            sb.append("\"name\":\"").append(escapeJson((String) t.get("name"))).append("\",");
            sb.append("\"seq\":").append(t.get("seq")); // raw JSON stored in DB
            sb.append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    private void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";

        Path file = uiRoot.resolve(path.substring(1)).normalize();

        // security: stay inside uiRoot
        if (!file.startsWith(uiRoot) || !Files.exists(file) || Files.isDirectory(file)) {
            byte[] msg = "404 Not Found".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(404, msg.length);
            ex.getResponseBody().write(msg);
            ex.getResponseBody().close();
            return;
        }

        String mime = mimeFor(file.toString());
        byte[] data = Files.readAllBytes(file);
        ex.getResponseHeaders().add("Content-Type", mime);
        addCors(ex);
        ex.sendResponseHeaders(200, data.length);
        ex.getResponseBody().write(data);
        ex.getResponseBody().close();
    }

    // ── CORS / OPTIONS ───────────────────────────────────────────────────────

    /** Returns true (and sends 204) if this is an OPTIONS preflight. */
    private boolean isPreflight(HttpExchange ex) throws IOException {
        addCors(ex);
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private void addCors(HttpExchange ex) {
        ex.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    // ── utilities ────────────────────────────────────────────────────────────

    private void sendJson(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        addCors(ex);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.getResponseBody().close();
    }

    private List<String> extractJsonArray(String json, String key) {
        String marker = "\"" + key + "\":[";
        int start = json.indexOf(marker);
        if (start < 0) return new ArrayList<>();
        start += marker.length();
        int end = json.indexOf("]", start);
        if (end < 0) return new ArrayList<>();
        String content = json.substring(start, end).trim();
        List<String> result = new ArrayList<>();
        if (content.isEmpty()) return result;
        for (String part : content.split(",")) {
            String v = part.trim();
            if (v.startsWith("\"") && v.endsWith("\""))
                result.add(v.substring(1, v.length() - 1));
        }
        return result;
    }

    private int extractJsonInt(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) return 0;
        start += marker.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        if (start == end) return 0;
        try { return Integer.parseInt(json.substring(start, end)); } catch (NumberFormatException e) { return 0; }
    }

    private double extractJsonDouble(String json, String key) {
        String marker = "\"" + key + "\":";
        int start = json.indexOf(marker);
        if (start < 0) return 0.0;
        start += marker.length();
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) end++;
        if (start == end) return 0.0;
        try { return Double.parseDouble(json.substring(start, end)); } catch (NumberFormatException e) { return 0.0; }
    }

    private String extractQueryParam(String query, String key) {
        if (query == null) return "";
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(key))
                return pair.substring(eq + 1);
        }
        return "";
    }

    private String toJsonStringArray(List<String> items) {
        if (items == null || items.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escapeJson(items.get(i))).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    private String nullStr(Object o) { return o == null ? "" : (String) o; }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractJsonString(String json, String key) {
        String marker = "\"" + key + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return "";
        start += marker.length();
        int end = json.indexOf("\"", start);
        return end < 0 ? "" : json.substring(start, end);
    }

    private String mimeFor(String name) {
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".css"))  return "text/css; charset=utf-8";
        if (name.endsWith(".js") || name.endsWith(".jsx")) return "text/javascript; charset=utf-8";
        if (name.endsWith(".json")) return "application/json; charset=utf-8";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }

    @FunctionalInterface
    private interface Handler { void handle(HttpExchange ex) throws IOException; }

    private void handle(HttpExchange ex, Handler h) {
        try { h.handle(ex); } catch (Exception e) {
            System.err.println("Handler error: " + e.getMessage());
            try { ex.sendResponseHeaders(500, -1); } catch (IOException ignored) {}
        }
    }
}