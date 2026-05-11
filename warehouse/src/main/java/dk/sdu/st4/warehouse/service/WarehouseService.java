package dk.sdu.st4.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.services.IWarehouse;

public class WarehouseService implements IWarehouse {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final WarehouseModel model;

    public WarehouseService() {
        this(new WarehouseConnect(AppConfig.WAREHOUSE_SERVICE_URL).getModel());
    }

    public WarehouseService(WarehouseModel model) {
        this.model = model;
    }

    @Override
    public void PickItem(int trayID, String machineID) {
        model.proxy.pickItem(trayID);
    }

    @Override
    public void InsertItem(int trayID, String name, String machineID) {
        model.proxy.insertItem(trayID, name);
    }

    @Override
    public void GetInventory(String machineID) {
        model.proxy.getInventory();
    }

    @Override
    public int GetState(String machineID) {
        try {
            String json = model.proxy.getInventory();
            JsonNode root = mapper.readTree(json);
            return root.get("State").asInt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read State from warehouse " + machineID, e);
        }
    }
}
