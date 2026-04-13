package dk.sdu.st4.core.enums;

/**
 * Operational states reported by the Warehouse SOAP service.
 *
 * Appears in the GetInventory response under "State" (integer value).
 */
public enum WarehouseState {

    IDLE(0),
    EXECUTING(1),
    ERROR(2);

    private final int code;

    WarehouseState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static WarehouseState fromCode(int code) {
        for (WarehouseState s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("Unknown Warehouse state code: " + code);
    }
}
