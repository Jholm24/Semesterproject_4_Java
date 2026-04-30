import dk.sdu.st4.warehouse.service.WarehouseService;

public class WarehouseClientSmokeTest {

    public static void main(String[] args) throws Exception {
        String machineSerialNumber = "WH-123";
        String baseUrl = "http://localhost:8087/Service.asmx";

        WarehouseService service = new WarehouseService(baseUrl);

        System.out.println("--- Henter inventory ---");
        service.GetInventory(machineSerialNumber);

        System.out.println("--- GetState: " + service.GetState(machineSerialNumber) + " ---");

        System.out.println("--- Smoke test OK ---");
    }
}
