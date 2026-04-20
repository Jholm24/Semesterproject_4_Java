package dk.sdu.st4.core.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server (JDK HttpServer) that:
 *  - serves the React UI from core/ui/ as static files
 *  - exposes /api/status, /api/events, /api/control for the UI to call
 */
public class ApiServer {

    private final Orchestrator orchestrator;
    private final int          port;
    private final Path         uiRoot;
    private HttpServer         server;

    public ApiServer(Orchestrator orchestrator, int port, Path uiRoot) {
        this.orchestrator = orchestrator;
        this.port   = port;
        this.uiRoot = uiRoot;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/status",  ex -> handle(ex, this::handleStatus));
        server.createContext("/api/events",  ex -> handle(ex, this::handleEvents));
        server.createContext("/api/control", ex -> handle(ex, this::handleControl));
        server.createContext("/",            ex -> handle(ex, this::handleStatic));
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
            "{\"agv\":%s,\"lineStatus\":\"%s\"}",
            orchestrator.agvJson(), orchestrator.getLineStatus()
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