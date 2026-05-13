package dk.sdu.st4.warehouse.service;

import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.services.IWarehouse;
import dk.sdu.st4.common.services.spi.IWarehouseFactory;

public class WarehouseSoapFactory implements IWarehouseFactory {

    @Override
    public String variant() {
        return "soap";
    }

    @Override
    public IWarehouse create(String serialNo, String baseUrl) {
        WarehouseConnect connect = new WarehouseConnect(
                baseUrl != null ? baseUrl : AppConfig.WAREHOUSE_SERVICE_URL);
        connect.connectMachine(serialNo);
        return new WarehouseService(connect.getModel());
    }
}
