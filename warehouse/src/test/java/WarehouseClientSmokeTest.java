import dk.sdu.st4.warehouse.service.WarehouseClient;
public class WarehouseClientSmokeTest {

    public static void main(String[] args) throws Exception {
        WarehouseClient client = WarehouseClient.getInstance();

        String machineSerialNumber = "WH-P42069"; // ændre værdi for at tilføje andre
        String base_url = "http://localhost:8087/Service.asmx"; // ændre værdi for at tilføje andre
        String type = "warehouse";
        String variant = "Parts";



        System.out.println("--- Tilføjer maskine til DB ---");
        client.addMachine(machineSerialNumber,type,variant, base_url);

        System.out.println("--- Connecter til maskine ---");
        client.connectMachine(machineSerialNumber).get(); // .get() venter på CompletableFuture

        System.out.println("--- Er connected: " + client.isConnected(machineSerialNumber) + " ---");

        System.out.println("--- Henter inventory ---");
        client.GetInventory(machineSerialNumber);

        System.out.println("--- GetState: " + client.GetState(machineSerialNumber) + " ---");

        System.out.println("--- Smoke test OK ---");
    }
}
