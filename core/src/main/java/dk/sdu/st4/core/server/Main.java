package dk.sdu.st4.core.server;

import dk.sdu.st4.common.db.DbLineRepository;
import dk.sdu.st4.common.services.IAgvRegistry;
import dk.sdu.st4.common.services.IAssemblyRegistry;
import dk.sdu.st4.common.services.IWarehouseRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class Main {

    public static void main(String[] args) throws Exception {
        int  port   = Integer.parseInt(System.getProperty("server.port", "8080"));
        Path uiRoot = Path.of(System.getProperty("ui.path",
            "core/src/main/java/dk/sdu/st4/core/ui")).toAbsolutePath();

        AnnotationConfigApplicationContext ctx =
                new AnnotationConfigApplicationContext(ModuleConfig.class);

        @SuppressWarnings("unchecked")
        Optional<IAgvRegistry>       agvRegistry       = (Optional<IAgvRegistry>)       ctx.getBean("agvRegistry");
        @SuppressWarnings("unchecked")
        Optional<IWarehouseRegistry> warehouseRegistry = (Optional<IWarehouseRegistry>) ctx.getBean("warehouseRegistry");
        @SuppressWarnings("unchecked")
        Optional<IAssemblyRegistry>  assemblyRegistry  = (Optional<IAssemblyRegistry>)  ctx.getBean("assemblyRegistry");

        java.util.List<java.util.Map<String, Object>> allLines = DbLineRepository.getAllLines();

        String lineId = allLines.stream()
                .map(l -> (String) l.get("id"))
                .findFirst().orElse(null);

        ProductionOrchestrator orchestrator = new ProductionOrchestrator(
                agvRegistry, warehouseRegistry, assemblyRegistry, lineId);

        // Reconnect machines that were already assigned to lines before this server restart
        java.util.Set<String> seenSerials = new java.util.HashSet<>();
        for (java.util.Map<String, Object> line : allLines) {
            @SuppressWarnings("unchecked")
            java.util.List<String> machines = (java.util.List<String>) line.get("machines");
            if (machines != null) {
                java.util.List<String> newOnes = machines.stream()
                        .filter(seenSerials::add)
                        .toList();
                if (!newOnes.isEmpty()) orchestrator.onMachinesAssigned(newOnes);
            }
        }

        ApiServer server = new ApiServer(orchestrator, port, uiRoot);
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            ctx.close();
        }));

        System.out.println("=================================================");
        System.out.println("  Skateboard Productions");
        System.out.println("  http://localhost:" + port);
        System.out.println("  UI root : " + uiRoot);
        System.out.println("  Modules :");
        System.out.println("    AGV       : " + (agvRegistry.isPresent()       ? "loaded" : "MISSING"));
        System.out.println("    Warehouse : " + (warehouseRegistry.isPresent() ? "loaded" : "MISSING"));
        System.out.println("    Assembly  : " + (assemblyRegistry.isPresent()  ? "loaded" : "MISSING"));
        System.out.println("  Requires: docker compose up -d");
        System.out.println("=================================================");

        // Exit when stdin closes (terminal window closed)
        try {
            while (System.in.read() != -1) {}
        } catch (IOException ignored) {}
        System.exit(0);

    }
}
