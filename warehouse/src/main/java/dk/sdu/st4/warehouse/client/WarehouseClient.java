package dk.sdu.st4.warehouse.client;

import dk.sdu.st4.core.exception.WarehouseException;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Low-level SOAP/HTTP client for the Warehouse service.
 *
 * <p>All three operations ({@code PickItem}, {@code InsertItem}, {@code GetInventory}) share
 * the same service endpoint and follow the standard SOAP-over-HTTP pattern:
 * <ul>
 *   <li>HTTP method: POST</li>
 *   <li>Content-Type: {@code text/xml; charset=utf-8}</li>
 *   <li>SOAPAction header: {@code "http://tempuri.org/<OperationName>"}</li>
 *   <li>Body: SOAP 1.1 envelope wrapping the operation element</li>
 * </ul>
 *
 * <p>Service endpoint: {@code http://localhost:8081/Service.asmx}
 * <p>WSDL: {@code http://localhost:8081/Service.asmx?wsdl}
 *
 * <p><b>Alternative:</b> Use {@code wsimport} to auto-generate JAX-WS stubs from the WSDL
 * and replace this manual client with the generated proxy.
 */
public class WarehouseClient {

    private final HttpClient httpClient;
    private final URI serviceUri;

    public WarehouseClient(String serviceUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.serviceUri = URI.create(serviceUrl);
    }

    /**
     * Sends a SOAP request and returns the raw XML response body.
     *
     * @param soapAction  the value for the {@code SOAPAction} HTTP header
     *                    (e.g. {@code "http://tempuri.org/PickItem"})
     * @param soapEnvelope complete SOAP 1.1 envelope XML string
     * @return raw XML response body from the service
     * @throws WarehouseException if the HTTP call fails or the service returns a non-200 status
     */
    public String sendSoapRequest(String soapAction, String soapEnvelope) throws WarehouseException {
        // TODO:
        //  1. Build HttpRequest:
        //       POST serviceUri
        //       Header "Content-Type": "text/xml; charset=utf-8"
        //       Header "SOAPAction": soapAction
        //       Body: soapEnvelope (BodyPublishers.ofString)
        //  2. Send via httpClient.send(request, BodyHandlers.ofString())
        //  3. Verify HTTP status 200; throw WarehouseException on failure
        //  4. Return response body string
        throw new UnsupportedOperationException("TODO: implement WarehouseClient.sendSoapRequest");
    }

    // -------------------------------------------------------------------------
    // SOAP envelope helpers (static factory methods)
    // -------------------------------------------------------------------------

    /**
     * Builds the SOAP 1.1 envelope for the {@code PickItem} operation.
     *
     * @param trayId the tray to retrieve
     * @return well-formed SOAP envelope XML string
     */
    public static String buildPickItemEnvelope(int trayId) {
        // TODO:
        //  Return a SOAP 1.1 envelope of the form:
        //  <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
        //                 xmlns:tns="http://tempuri.org/">
        //    <soap:Body>
        //      <tns:PickItem><tns:trayId>{trayId}</tns:trayId></tns:PickItem>
        //    </soap:Body>
        //  </soap:Envelope>
        throw new UnsupportedOperationException("TODO: implement buildPickItemEnvelope");
    }

    /**
     * Builds the SOAP 1.1 envelope for the {@code InsertItem} operation.
     *
     * @param trayId the target tray
     * @param name   item name to store
     * @return well-formed SOAP envelope XML string
     */
    public static String buildInsertItemEnvelope(int trayId, String name) {
        // TODO:
        //  Similar to buildPickItemEnvelope but with trayId + name elements.
        throw new UnsupportedOperationException("TODO: implement buildInsertItemEnvelope");
    }

    /**
     * Builds the SOAP 1.1 envelope for the {@code GetInventory} operation.
     *
     * @return well-formed SOAP envelope XML string
     */
    public static String buildGetInventoryEnvelope() {
        // TODO:
        //  <tns:GetInventory/> with no child elements.
        throw new UnsupportedOperationException("TODO: implement buildGetInventoryEnvelope");
    }
}
