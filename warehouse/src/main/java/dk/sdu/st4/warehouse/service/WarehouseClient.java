package dk.sdu.st4.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.st4.common.services.IWarehouse;

public class WarehouseClient implements IWarehouse {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final WarehouseConnect connect;

    public WarehouseClient(WarehouseConnect connect) {
        this.connect = connect;
    }

    @Override
    public void PickItem(int trayID, String serialNumber) {
        connect.getService(serialNumber).pickItem(trayID);
    }

    @Override
    public void InsertItem(int trayID, String name, String serialNumber) {
        connect.getService(serialNumber).insertItem(trayID, name);
    }

    @Override
    public void GetInventory(String serialNumber) {
        connect.getService(serialNumber).getInventory();
    }

    @Override
    public int GetState(String serialNumber) {
        try {
            String json = connect.getService(serialNumber).getInventory();
            JsonNode root = mapper.readTree(json);
            return root.get("State").asInt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read State from warehouse " + serialNumber, e);
        }
    }
}
