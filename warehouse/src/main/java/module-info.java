import dk.sdu.st4.common.services.IConnect;
import dk.sdu.st4.common.services.IWarehouse;
import dk.sdu.st4.common.services.IWarehouseRegistry;
import dk.sdu.st4.warehouse.service.WarehouseConnect;
import dk.sdu.st4.warehouse.service.WarehouseRegistry;
import dk.sdu.st4.warehouse.service.WarehouseService;

module dk.sdu.st4.warehouse {
    exports dk.sdu.st4.warehouse.service;
    opens dk.sdu.st4.warehouse.service;
    requires java.logging;
    requires com.fasterxml.jackson.databind;
    requires jakarta.xml.bind;
    requires jakarta.jws;
    requires jakarta.xml.ws;
    requires dk.sdu.st4.common;
    requires org.glassfish.jaxb.core;
    requires java.sql;

    provides IWarehouse         with WarehouseService;
    provides IConnect           with WarehouseConnect;
    provides IWarehouseRegistry with WarehouseRegistry;
}
