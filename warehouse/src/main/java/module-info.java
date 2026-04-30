import dk.sdu.st4.warehouse.service.WarehouseService;

module dk.sdu.st4.warehouse {
    exports dk.sdu.st4.warehouse.service;
    requires java.logging;
    requires com.fasterxml.jackson.databind;
    requires jakarta.xml.bind;
    requires jakarta.jws;
    requires jakarta.xml.ws;
    requires dk.sdu.st4.common;
    requires org.glassfish.jaxb.core;

    provides dk.sdu.st4.common.services.IWarehouse
            with WarehouseService;
}
