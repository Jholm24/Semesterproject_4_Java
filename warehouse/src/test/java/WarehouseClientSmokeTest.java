import dk.sdu.st4.warehouse.service.WarehouseClient;

public class WarehouseClientSmokeTest {

    public static void main(String[] args) throws Exception {
        WarehouseClient client = WarehouseClient.getInstance();

        int machineId = 1; // ændre værdi for at tilføje andre
        String url = "http://localhost:8081/Service.asmx"; // ændre værdi for at tilføje andre

        System.out.println("--- Tilføjer maskine til DB ---");
        client.addMachine(machineId, url, "Parts");

        System.out.println("--- Connecter til maskine ---");
        client.connectMachine(machineId).get(); // .get() venter på CompletableFuture

        System.out.println("--- Er connected: " + client.isConnected(machineId) + " ---");

        System.out.println("--- Henter inventory ---");
        client.GetInventory(machineId);

        System.out.println("--- GetState: " + client.GetState(machineId) + " ---");

        System.out.println("--- Smoke test OK ---");
    }
}
