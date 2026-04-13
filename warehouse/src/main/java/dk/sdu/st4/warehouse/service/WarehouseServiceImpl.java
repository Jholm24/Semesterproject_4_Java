package dk.sdu.st4.warehouse.service;

import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.core.exception.WarehouseException;
import dk.sdu.st4.core.model.WarehouseInventory;
import dk.sdu.st4.core.service.IWarehouseService;
import dk.sdu.st4.warehouse.client.WarehouseClient;

/**
 * SOAP-backed implementation of {@link IWarehouseService}.
 *
 * <p>Each method:
 * <ol>
 *   <li>Builds the appropriate SOAP envelope via {@link WarehouseClient}.</li>
 *   <li>Sends it using {@link WarehouseClient#sendSoapRequest(String, String)}.</li>
 *   <li>Parses the XML response where applicable (e.g. {@link #getInventory()}).</li>
 * </ol>
 */
public class WarehouseServiceImpl implements IWarehouseService {

    private final WarehouseClient client;

    /** Creates a service instance targeting the default warehouse URL from {@link AppConfig}. */
    public WarehouseServiceImpl() {
        this(AppConfig.WAREHOUSE_SERVICE_URL);
    }

    /** Creates a service instance targeting a custom service URL (useful for testing). */
    public WarehouseServiceImpl(String serviceUrl) {
        this.client = new WarehouseClient(serviceUrl);
    }

    /** {@inheritDoc} */
    @Override
    public void pickItem(int trayId) throws WarehouseException {
        // TODO:
        //  1. Build SOAP envelope: WarehouseClient.buildPickItemEnvelope(trayId)
        //  2. Send: client.sendSoapRequest("http://tempuri.org/PickItem", envelope)
        //  3. (Optional) Parse response XML to confirm success or detect fault elements
        throw new UnsupportedOperationException("TODO: implement WarehouseServiceImpl.pickItem");
    }

    /** {@inheritDoc} */
    @Override
    public void insertItem(int trayId, String name) throws WarehouseException {
        // TODO:
        //  1. Build SOAP envelope: WarehouseClient.buildInsertItemEnvelope(trayId, name)
        //  2. Send: client.sendSoapRequest("http://tempuri.org/InsertItem", envelope)
        //  3. (Optional) Parse response to confirm success
        throw new UnsupportedOperationException("TODO: implement WarehouseServiceImpl.insertItem");
    }

    /** {@inheritDoc} */
    @Override
    public WarehouseInventory getInventory() throws WarehouseException {
        // TODO:
        //  1. Build SOAP envelope: WarehouseClient.buildGetInventoryEnvelope()
        //  2. Send: client.sendSoapRequest("http://tempuri.org/GetInventory", envelope)
        //  3. Parse XML response:
        //     - Extract <Inventory> child elements (key = trayId, value = item name)
        //     - Extract <State> integer → WarehouseState.fromCode(...)
        //     - Extract <TimeStamp> string
        //  4. Return new WarehouseInventory(inventoryMap, state, timestamp)
        throw new UnsupportedOperationException("TODO: implement WarehouseServiceImpl.getInventory");
    }
}
