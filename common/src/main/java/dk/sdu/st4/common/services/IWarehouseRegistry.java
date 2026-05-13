package dk.sdu.st4.common.services;

import java.util.List;
import java.util.Map;

public interface IWarehouseRegistry {
    void loadFromDb();
    void connect(String serialNumber);
    void disconnect(String serialNumber);
    List<Map<String, Object>> getMachinesStatus();
    IWarehouse getWarehouse(String serialNumber);
    IWarehouse getWarehouseByVariant(String variant);
}
