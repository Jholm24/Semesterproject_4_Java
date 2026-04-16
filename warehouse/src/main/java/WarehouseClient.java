import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.st4.warehouse.service.IEmulatorService;
import dk.sdu.st4.warehouse.service.IEmulatorService_Service;
import dk.sdu.st4.common.Interfaces.IWarehouse;
import dk.sdu.st4.common.Interfaces.IConnect;

import java.util.concurrent.CompletableFuture;

public class WarehouseClient implements IWarehouse, IConnect {
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

    @Override
    public int getMachineId() {
        return 0;
    }

    @Override
    public void setMachineId(int machineId) {

    }

    @Override
    public String getMachineType() {
        return "";
    }

    @Override
    public void setMachineType(String machineType) {

    }

    @Override
    public void addMachine(int machineId, String machineType) {


    }

    @Override
    public void removeMachine(int machineId) {

    }

    @Override
    public CompletableFuture<Void> connectMachine(int machineId) {
        return null;
    }

    @Override
    public void disconnectMachine(int machineId) {

    }

    @Override
    public boolean isConnected(int machineId) {
        return false;
    }
}
