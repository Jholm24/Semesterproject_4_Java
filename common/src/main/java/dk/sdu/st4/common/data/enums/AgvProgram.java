package dk.sdu.st4.common.data.enums;

/**
 * Named programs that can be loaded and executed on the AGV.
 * Each constant stores the exact string the REST API expects in "Program name".
 *
 * AGV REST API: PUT http://localhost:8082/v1/status/
 */
public enum AgvProgram {

    /** Drive the AGV to the charging dock. */
    MoveToChargerOperation("Move the AGV to the charging station."),

    /** Drive the AGV to the assembly station. */
    MoveToAssemblyOperation("Move the AGV to the assembly station."),

    /** Drive the AGV to the warehouse. */
    MoveToStorageOperation("Move the AGV to the warehouse."),

    /** Robot arm picks payload from the AGV and places it at the assembly station. */
    PutAssemblyOperation("Activate the robot arm to pick payload from AGV and place it at the assembly station."),

    /** Robot arm picks payload at the assembly station and places it on the AGV. */
    PickAssemblyOperation("Activate the robot arm to pick payload at the assembly station and place it on the AGV."),

    /** Robot arm picks payload from the warehouse outlet onto the AGV. */
    PickWarehouseOperation("Activate the robot arm to pick payload from the warehouse outlet."),

    /** Robot arm places payload from the AGV into the warehouse inlet. */
    PutWarehouseOperation("Activate the robot arm to place an item at the warehouse inlet.");

    /** Exact program name string sent in the REST request body. */
    private final String program;

    AgvProgram(String program) {
        this.program = program;
    }

    public String getProgram() {
        return program;
    }
}
