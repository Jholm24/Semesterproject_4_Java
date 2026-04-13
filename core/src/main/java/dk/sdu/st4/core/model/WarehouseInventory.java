package dk.sdu.st4.core.model;

import dk.sdu.st4.core.enums.WarehouseState;

import java.util.Map;

/**
 * Inventory snapshot returned by the Warehouse SOAP {@code GetInventory} operation.
 *
 * Maps to the XML response payload:
 * <pre>
 * {
 *   "Inventory": [{"1": "Item 1", "2": "Item 2", "3": "Item 3", "4": "Assembly 1", "5": ""}],
 *   "State":     0,
 *   "TimeStamp": "12:34:56"
 * }
 * </pre>
 *
 * Keys in {@code inventory} are tray IDs; values are item names (empty string = empty tray).
 */
public class WarehouseInventory {

    /** Map of tray ID → item name. Empty string means the tray is unoccupied. */
    private Map<Integer, String> inventory;

    /** Current operational state of the warehouse PLC. */
    private WarehouseState state;

    /** Timestamp of the inventory snapshot as reported by the warehouse. */
    private String timestamp;

    public WarehouseInventory() {}

    public WarehouseInventory(Map<Integer, String> inventory, WarehouseState state, String timestamp) {
        this.inventory = inventory;
        this.state = state;
        this.timestamp = timestamp;
    }

    public Map<Integer, String> getInventory() { return inventory; }
    public void setInventory(Map<Integer, String> inventory) { this.inventory = inventory; }

    public WarehouseState getState() { return state; }
    public void setState(WarehouseState state) { this.state = state; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "WarehouseInventory{inventory=" + inventory + ", state=" + state
                + ", timestamp='" + timestamp + "'}";
    }
}
