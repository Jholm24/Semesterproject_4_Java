package dk.sdu.st4.core.service;

import dk.sdu.st4.core.exception.WarehouseException;
import dk.sdu.st4.core.model.WarehouseInventory;

/**
 * Contract for communicating with the Warehouse via its SOAP API.
 *
 * <p>WSDL / service endpoint: {@code http://localhost:8081/Service.asmx}
 *
 * <p>The warehouse operates on removable boxes placed in non-removable trays.
 * Tray IDs are 1-based integers (e.g. 1–5 for a five-tray configuration).
 */
public interface IWarehouseService {

    /**
     * Requests the warehouse PLC to move the box at {@code trayId} to the outlet
     * so the AGV can pick it up.
     *
     * @param trayId the ID of the tray to retrieve
     * @throws WarehouseException if the SOAP call fails or the warehouse is in ERROR state
     */
    void pickItem(int trayId) throws WarehouseException;

    /**
     * Inserts (stores) an item into the warehouse at the given tray position.
     *
     * @param trayId the target tray ID
     * @param name   human-readable item/product name to record
     * @throws WarehouseException if the SOAP call fails or the tray is occupied
     */
    void insertItem(int trayId, String name) throws WarehouseException;

    /**
     * Fetches the complete current inventory along with the warehouse state and timestamp.
     *
     * @return a {@link WarehouseInventory} snapshot
     * @throws WarehouseException if the SOAP call fails
     */
    WarehouseInventory getInventory() throws WarehouseException;
}
