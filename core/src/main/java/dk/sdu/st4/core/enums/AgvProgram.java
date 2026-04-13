package dk.sdu.st4.core.enums;

/**
 * Named programs that can be loaded and executed on the AGV.
 * Each constant stores the exact string the REST API expects in "Program name".
 *
 * AGV REST API: PUT http://localhost:8082/v1/status/
 */
public enum AgvProgram {

    /** Drive the AGV to the charging dock. */
    MOVE_TO_CHARGER("MoveToChargerOperation"),

    /** Drive the AGV to the assembly station. */
    MOVE_TO_ASSEMBLY("MoveToAssemblyOperation"),

    /** Drive the AGV to the warehouse. */
    MOVE_TO_STORAGE("MoveToStorageOperation"),

    /** Robot arm picks payload from the AGV and places it at the assembly station. */
    PUT_ASSEMBLY("PutAssemblyOperation"),

    /** Robot arm picks payload at the assembly station and places it on the AGV. */
    PICK_ASSEMBLY("PickAssemblyOperation"),

    /** Robot arm picks payload from the warehouse outlet onto the AGV. */
    PICK_WAREHOUSE("PickWarehouseOperation"),

    /** Robot arm places payload from the AGV into the warehouse inlet. */
    PUT_WAREHOUSE("PutWarehouseOperation");

    /** Exact program name string sent in the REST request body. */
    private final String apiName;

    AgvProgram(String apiName) {
        this.apiName = apiName;
    }

    public String getApiName() {
        return apiName;
    }
}
