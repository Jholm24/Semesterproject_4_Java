package dk.sdu.st4.common.services;

public interface IWarehouseRegistry {
    void loadFromDb();
    void connect(String serialNumber);
    void disconnect(String serialNumber);
    IWarehouse getWarehouse(String serialNumber);
}
