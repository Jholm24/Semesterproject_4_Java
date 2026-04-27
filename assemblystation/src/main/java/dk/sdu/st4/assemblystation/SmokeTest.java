package dk.sdu.st4.assemblystation;

import dk.sdu.st4.common.services.IConnect;

import java.util.ArrayList;
import java.util.List;

public class SmokeTest {
    public static void main(String[] args) throws Exception {

        System.out.println("=== Assembly Smoke Test (Multi-Connection) ===\n");

        // 1. Hent registry og konfigurer fra DB
        AssemblyRegistry registry = AssemblyRegistry.getInstance();
        registry.configure();
        System.out.println("✓ Registry configured fra DB\n");

        // 2. Forbind ALLE ledige maskiner
        List<String> connectedKeys = new ArrayList<>();
        List<AssemblyController> controllers = new ArrayList<>();
        int index = 1;

        while (true) {
            IConnect machine = registry.connectNext();
            if (machine == null) break;

            String key = "assembly-" + index++;
            connectedKeys.add(key);
            controllers.add((AssemblyController) machine);
            System.out.println("✓ Forbundet: " + machine.getMachineId());
        }

        if (controllers.isEmpty()) {
            System.out.println("✗ Ingen maskiner tilgængelige i DB");
            return;
        }

        System.out.println("\n→ " + controllers.size() + " maskiner forbundet. Venter på MQTT...");
        Thread.sleep(2000);

        // 3. Tjek status på alle maskiner
        System.out.println("\n--- Status check ---");
        for (int i = 0; i < controllers.size(); i++) {
            AssemblyController ctrl = controllers.get(i);
            String key = connectedKeys.get(i);

            boolean connected = ctrl.isConnected(ctrl.getMachineId());
            int state        = ctrl.getStatus();
            boolean healthy  = ctrl.getHealth();
            int operation    = ctrl.getOperation();

            System.out.printf("[%s] connected=%-5b | state=%-2d | healthy=%-5b | operation=%d%n",
                    key, connected, state, healthy, operation);
        }

        // 4. Send operation til alle maskiner
        System.out.println("\n--- Sender operationer ---");
        for (int i = 0; i < controllers.size(); i++) {
            controllers.get(i).setExecuteOperation();
            System.out.println("✓ setExecuteOperation() sendt til " + connectedKeys.get(i));
        }

        Thread.sleep(1000);

        // 5. Aflæs LastOperationId for alle
        System.out.println("\n--- LastOperationId efter operation ---");
        for (int i = 0; i < controllers.size(); i++) {
            System.out.printf("[%s] lastOperationId=%d%n",
                    connectedKeys.get(i), controllers.get(i).getLastOperationId());
        }

        // 6. Disconnect alle
        System.out.println("\n--- Disconnecter alle ---");
        for (String key : connectedKeys) {
            registry.disconnect(key);
            System.out.println("✓ Disconnected: " + key);
        }

        System.out.println("\n=== Smoke Test Færdig ===");
    }
}