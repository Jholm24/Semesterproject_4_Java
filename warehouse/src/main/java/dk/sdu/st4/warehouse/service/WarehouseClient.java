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
    public void PickItem(int trayID, int machineSerialNumber) {
        connect.getConnection(machineSerialNumber).pickItem(trayID);
    }

    @Override
    public void InsertItem(int trayID, String name, int machineSerialNumber) {
        connect.getConnection(machineSerialNumber).insertItem(trayID, name);
    }

    @Override
    public void GetInventory(int machineSerialNumber) {
        connect.getConnection(machineSerialNumber).getInventory();
    }

    @Override
    public int GetState(int machineSerialNumber) {
        try {
            String json = connect.getConnection(machineSerialNumber).getInventory();
            JsonNode root = mapper.readTree(json);
            return root.get("State").asInt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read State from warehouse " + machineSerialNumber, e);
        }
    }
}
