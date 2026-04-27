package dk.sdu.st4.assemblystation;

public class SmokeTest {
    public static void main(String[] args) throws Exception {

        System.out.println("=== Assembly Smoke Test ===");

        // 1. Hent registry og konfigurer fra DB
        AssemblyRegistry registry = AssemblyRegistry.getInstance();
        registry.configure();
        System.out.println("✓ Registry configured fra DB");

        // 2. Forbind næste ledige maskine
        var machine = registry.connectNext();
        if (machine == null) {
            System.out.println("✗ Ingen maskiner tilgængelige i DB");
            return;
        }
        System.out.println("✓ Forbundet til maskine: " + machine.getMachineId());

        // 3. Vent lidt på MQTT-forbindelsen er klar
        Thread.sleep(2000);

        // 4. Tjek at maskinen er connected
        if (machine.isConnected(machine.getMachineId())) {
            System.out.println("✓ isConnected = true");
        } else {
            System.out.println("✗ isConnected = false");
        }

        // 5. Cast til AssemblyController for at tjekke state og health
        AssemblyController controller = (AssemblyController) machine;

        System.out.println("✓ State: " + controller.getStatus());
        System.out.println("✓ Health: " + controller.getHealth());
        System.out.println("✓ CurrentOperation: " + controller.getOperation());

        // 6. Send en operation
        controller.setExecuteOperation();
        System.out.println("✓ setExecuteOperation() sendt");

        Thread.sleep(1000);
        System.out.println("✓ LastOperationId: " + controller.getLastOperationId());

        // 7. Disconnect
        registry.disconnect("assembly-1");
        System.out.println("✓ Disconnected");

        System.out.println("=== Smoke Test Færdig ===");
    }
}
