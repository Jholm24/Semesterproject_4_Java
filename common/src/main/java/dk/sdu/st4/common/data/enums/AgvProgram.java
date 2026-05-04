package dk.sdu.st4.common.data.enums;

/**
 * Named programs that can be loaded and executed on the AGV.
 * Each constant stores the exact string the REST API expects in "Program name".
 *
 * AGV REST API: PUT http://localhost:8082/v1/status/
 */
public enum AgvProgram {

    MoveToChargerOperation,
    MoveToAssemblyOperation,
    MoveToStorageOperation,
    PutAssemblyOperation,
    PickAssemblyOperation,
    PickWarehouseOperation,
    PutWarehouseOperation;

    /** Returns the exact program name the AGV REST API expects — which is the enum constant name. */
    public String getProgram() {
        return this.name();
    }
}
