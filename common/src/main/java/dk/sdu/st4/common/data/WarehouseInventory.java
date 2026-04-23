package dk.sdu.st4.common.data;

import dk.sdu.st4.common.data.enums.WarehouseState;

import java.util.Map;

public class WarehouseInventory {

    private Map<Integer, String> inventory;
    private WarehouseState state;
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
