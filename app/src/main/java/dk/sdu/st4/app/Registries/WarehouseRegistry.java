package dk.sdu.st4.app.Registries;

import dk.sdu.st4.common.services.IWarehouse;
import dk.sdu.st4.warehouse.service.WarehouseClient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class WarehouseRegistry {

    private final WarehouseClient client = WarehouseClient.getInstance();
    private final Queue<String> available = new LinkedList<>();
    private final Map<String, String> active = new HashMap<>();

    public void add(String serialNumber) {
        client.connectMachine(serialNumber);
        available.add(serialNumber);
    }

    public String acquire() {
        String serialNumber = available.poll();
        if (serialNumber == null) return null;
        active.put("warehouse-" + (active.size() + 1), serialNumber);
        return serialNumber;
    }

    public void release(String serialNumber) {
        active.values().remove(serialNumber);
        available.add(serialNumber);
    }

    public IWarehouse getClient() {
        return client;
    }
}