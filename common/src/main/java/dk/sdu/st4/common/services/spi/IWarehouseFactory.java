package dk.sdu.st4.common.services.spi;

import dk.sdu.st4.common.services.IWarehouse;

public interface IWarehouseFactory {
    String variant();
    IWarehouse create(String serialNo, String baseUrl) throws Exception;
}
