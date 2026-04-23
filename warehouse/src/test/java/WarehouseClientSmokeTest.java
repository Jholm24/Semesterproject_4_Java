import dk.sdu.st4.warehouse.service.WarehouseClient;
import dk.sdu.st4.warehouse.service.WarehouseConnect;

public class WarehouseClientSmokeTest {

    public static void main(String[] args) throws Exception {
        WarehouseConnect connect = new WarehouseConnect();
        WarehouseClient client = new WarehouseClient(connect);

        int machineSerialNumber = 6; // ændre værdi for at tilføje andre
        String base_url = "http://localhost:8087/Service.asmx"; // ændre værdi for at tilføje andre
        String type = "warehouse";
        String variant = "Parts";

        System.out.println("--- Tilføjer maskine til DB ---");
        // connect.addMachine(machineSerialNumber, type, variant, base_url);

        System.out.println("--- Connecter til maskine ---");
        connect.connectMachine(machineSerialNumber).get(); // .get() venter på CompletableFuture

        System.out.println("--- Er connected: " + connect.isConnected(machineSerialNumber) + " ---");

        System.out.println("--- Henter inventory ---");
        client.GetInventory(machineSerialNumber);
        System.out.println();

        System.out.println("--- GetState: " + client.GetState(machineSerialNumber) + " ---");

        System.out.println("--- Smoke test OK ---");
    }
}
