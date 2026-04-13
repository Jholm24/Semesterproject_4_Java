/**
 * Warehouse module — SOAP client component.
 *
 * Communicates with the Warehouse PLC over SOAP/HTTP using the built-in
 * {@code java.net.http} client for transport and {@code java.xml} for XML handling.
 * Implements {@link dk.sdu.st4.core.service.IWarehouseService}.
 *
 * WSDL / service endpoint: http://localhost:8081/Service.asmx
 *
 * Note: For a fully generated stub approach, run wsimport against the WSDL and
 * add the resulting sources to this module. The manual SOAP approach used here
 * is sufficient for simple three-operation services.
 */
module dk.sdu.st4.warehouse {
    requires dk.sdu.st4.core;
    requires dk.sdu.st4.common;
    requires java.net.http;
    requires java.xml;

    exports dk.sdu.st4.warehouse.service;
}
