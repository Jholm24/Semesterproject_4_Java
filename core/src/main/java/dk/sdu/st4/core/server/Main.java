package dk.sdu.st4.core.server;

import java.nio.file.Path;

/**
 * Entry point for the DECKFLOW production line server.
 *
 * Run from the project root with:
 *   mvn exec:java -pl core
 *
 * Then open http://localhost:8080 in your browser.
 * Requires `docker compose up -d` for the AGV emulator (port 8082).
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int  port   = Integer.parseInt(System.getProperty("server.port", "8080"));
        Path uiRoot = Path.of(System.getProperty("ui.path",
            "core/src/main/java/dk/sdu/st4/core/ui")).toAbsolutePath();

        Orchestrator orchestrator = new Orchestrator();
        ApiServer    server       = new ApiServer(orchestrator, port, uiRoot);

        server.start();

        System.out.println("=================================================");
        System.out.println("  Skateboard Productions");
        System.out.println("  http://localhost:" + port);
        System.out.println("  UI root : " + uiRoot);
        System.out.println("  Requires: docker compose up -d");
        System.out.println("=================================================");

        // keep the main thread alive
        Thread.currentThread().join();
    }
}