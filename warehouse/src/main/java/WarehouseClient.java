import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.st4.warehouse.service.IEmulatorService;
import dk.sdu.st4.warehouse.service.IEmulatorService_Service;
import dk.sdu.st4.common.Interfaces.IWarehouse;
public class WarehouseClient implements IWarehouse {
    private final IEmulatorService port;
    private final ObjectMapper mapper = new ObjectMapper();

    public WarehouseClient () {
        IEmulatorService_Service factory = new IEmulatorService_Service();
        this.port = factory.getBasicHttpBindingIEmulatorService();
    }

    @Override
    public void PickItem(int trayID) {
        port.pickItem(trayID);
    }

    @Override
    public void InsertItem(int trayID, String name) {
        port.insertItem(trayID, name);
    }

    @Override
    public void GetInventory() {
        port.getInventory();
    }

    @Override
    public int GetState() {
        try {
            String json = port.getInventory();
            JsonNode root = mapper.readTree(json);
            return root.get("State").asInt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read State from inventory", e);
        }
    }
}
