/**
 * Warehouse module — SOAP client component.
 * <p>
 * Communicates with the Warehouse PLC over SOAP/HTTP using the built-in
 * {@code java.net.http} client for transport and {@code java.xml} for XML handling.
 * Implements {link dk.sdu.st4.core.service.IWarehouseService}.
 * <p>
 * WSDL / service endpoint: http://localhost:8087/Service.asmx
 * <p>
 * Note: For a fully generated stub approach, run wsimport against the WSDL and
 * add the resulting sources to this module. The manual SOAP approach used here
 * is sufficient for simple three-operation services.
 */
module dk.sdu.st4.warehouse {
    requires java.net.http;
    requires java.xml;
    requires java.logging;
    requires com.fasterxml.jackson.databind;
    requires jakarta.xml.bind;
    requires jakarta.jws;
    requires jakarta.xml.ws;
    requires dk.sdu.st4.common;
    requires org.glassfish.jaxb.core;
    requires java.sql;

}
