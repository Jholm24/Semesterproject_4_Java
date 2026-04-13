package dk.sdu.st4.app;

/**
 * Application entry point.
 *
 * <p>Start the Docker services before running:
 * <pre>
 *   docker compose up -d
 * </pre>
 *
 * <p>Run from project root (module path approach):
 * <pre>
 *   java --module-path target/modules \
 *        --module dk.sdu.st4.app/dk.sdu.st4.app.Main
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        // TODO:
        //  1. Instantiate the three services:
        //       IAgvService agvService = new AgvServiceImpl(AppConfig.AGV_BASE_URL);
        //       IWarehouseService warehouseService = new WarehouseServiceImpl(AppConfig.WAREHOUSE_SERVICE_URL);
        //       IAssemblyStationService assemblyService =
        //           new AssemblyStationServiceImpl(AppConfig.MQTT_BROKER_URL);
        //
        //  2. Register status/health callbacks on assemblyService for monitoring:
        //       assemblyService.subscribeToStatus(status -> System.out.println("Status: " + status));
        //       assemblyService.subscribeToHealthCheck(result -> System.out.println("Health: " + result));
        //
        //  3. Create the orchestrator and run a production cycle:
        //       ProductionOrchestrator orchestrator =
        //           new ProductionOrchestrator(agvService, warehouseService, assemblyService);
        //       orchestrator.runProductionCycle(
        //           /* trayId */            1,
        //           /* assembledItemName */ "AssembledDronePart",
        //           /* processId */         12345
        //       );
        //
        //  4. Disconnect cleanly:
        //       assemblyService.disconnect();
        throw new UnsupportedOperationException("TODO: implement Main.main");
    }
}
