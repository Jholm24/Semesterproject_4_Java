import dk.sdu.st4.warehouse.service.WarehouseConnect;
import dk.sdu.st4.warehouse.service.WarehouseService;

public class WarehouseClientSmokeTest {

    public static void main(String[] args) throws Exception {
        String machineSerialNumber = "WH-123";
        String baseUrl = "http://localhost:8087/Service.asmx";

        WarehouseConnect connect = new WarehouseConnect(baseUrl);
        connect.connectMachine(machineSerialNumber);

        WarehouseService service = new WarehouseService(connect.getModel());

        System.out.println("--- Henter inventory ---");
        service.GetInventory(machineSerialNumber);

        System.out.println("--- GetState: " + service.GetState(machineSerialNumber) + " ---");

        System.out.println("--- Smoke test OK ---");
    }
}
