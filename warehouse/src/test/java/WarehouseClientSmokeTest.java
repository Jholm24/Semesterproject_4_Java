public class WarehouseClientSmokeTest {

    public static void main(String[] args) throws Exception {
        WarehouseClient client = WarehouseClient.getInstance();

        int machineSerialNumber = 500; // ændre værdi for at tilføje andre
        String base_url = "http://localhost:8081/Service.asmx"; // ændre værdi for at tilføje andre
        String type = "warehouse";
        String variant = "parts";



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
