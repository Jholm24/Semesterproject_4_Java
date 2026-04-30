package dk.sdu.st4.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.services.IWarehouse;
import jakarta.xml.ws.BindingProvider;

public class WarehouseService implements IWarehouse {

    private static final ObjectMapper mapper = new ObjectMapper();
    private final IEmulatorService proxy;

    public WarehouseService() {
        this(AppConfig.WAREHOUSE_SERVICE_URL);
    }

    public WarehouseService(String baseUrl) {
        IEmulatorService_Service factory = new IEmulatorService_Service();
        IEmulatorService svc = factory.getBasicHttpBindingIEmulatorService();
        ((BindingProvider) svc).getRequestContext()
                .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, baseUrl);
        this.proxy = svc;
    }

    @Override
    public void PickItem(int trayID, String machineID) {
        proxy.pickItem(trayID);
    }

    @Override
    public void InsertItem(int trayID, String name, String machineID) {
        proxy.insertItem(trayID, name);
    }

    @Override
    public void GetInventory(String machineID) {
        proxy.getInventory();
    }

    @Override
    public int GetState(String machineID) {
        try {
            String json = proxy.getInventory();
            JsonNode root = mapper.readTree(json);
            return root.get("State").asInt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read State from warehouse " + machineID, e);
        }
    }
}
