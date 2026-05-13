package dk.sdu.st4.core.server;

import dk.sdu.st4.common.db.DbLineRepository;
import dk.sdu.st4.common.services.IAgvRegistry;
import dk.sdu.st4.common.services.IAssemblyRegistry;
import dk.sdu.st4.common.services.IWarehouseRegistry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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

        String lineId = DbLineRepository.getAllLines().stream()
                .map(l -> (String) l.get("id"))
                .findFirst().orElse(null);

        ProductionOrchestrator orchestrator = new ProductionOrchestrator(
                agvRegistry, warehouseRegistry, assemblyRegistry, lineId);

        ApiServer server = new ApiServer(orchestrator, port, uiRoot);
        server.start();

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

        Thread.currentThread().join();
    }
}
