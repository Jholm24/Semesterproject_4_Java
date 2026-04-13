package dk.sdu.st4.app.orchestration;

import dk.sdu.st4.core.enums.AgvProgram;
import dk.sdu.st4.core.exception.AgvException;
import dk.sdu.st4.core.exception.AssemblyStationException;
import dk.sdu.st4.core.exception.St4Exception;
import dk.sdu.st4.core.exception.WarehouseException;
import dk.sdu.st4.core.service.IAgvService;
import dk.sdu.st4.core.service.IAssemblyStationService;
import dk.sdu.st4.core.service.IWarehouseService;

/**
 * Coordinates the full I4.0 production cycle by composing the three asset services.
 *
 * <p>Production sequence (one full cycle):
 * <ol>
 *   <li>Warehouse releases the part tray to the outlet.</li>
 *   <li>AGV drives to warehouse, picks up the part, and transports it to the assembly station.</li>
 *   <li>Assembly station processes the part.</li>
 *   <li>AGV collects the assembled product and returns it to the warehouse.</li>
 *   <li>Warehouse stores the finished product.</li>
 * </ol>
 */
public class ProductionOrchestrator {

    private final IAgvService agvService;
    private final IWarehouseService warehouseService;
    private final IAssemblyStationService assemblyStationService;

    public ProductionOrchestrator(IAgvService agvService,
                                  IWarehouseService warehouseService,
                                  IAssemblyStationService assemblyStationService) {
        this.agvService = agvService;
        this.warehouseService = warehouseService;
        this.assemblyStationService = assemblyStationService;
    }

    // -------------------------------------------------------------------------
    // Full cycle
    // -------------------------------------------------------------------------

    /**
     * Executes a complete production cycle end-to-end.
     *
     * @param trayId            the warehouse tray ID that holds the raw part
     * @param assembledItemName the name to record when the product is returned to the warehouse
     * @param processId         the assembly process ID to submit to the assembly station
     * @throws St4Exception if any step in the cycle fails
     */
    public void runProductionCycle(int trayId, String assembledItemName, int processId)
            throws St4Exception {
        // TODO:
        //  1. requestPart(trayId)
        //  2. transportToAssembly()
        //  3. startAssembly(processId)
        //  4. waitForAssemblyCompletion()   ← poll assemblyStationService / await callback
        //  5. returnToWarehouse(trayId, assembledItemName)
        throw new UnsupportedOperationException("TODO: implement ProductionOrchestrator.runProductionCycle");
    }

    // -------------------------------------------------------------------------
    // Individual steps
    // -------------------------------------------------------------------------

    /**
     * Requests the warehouse to move the part at {@code trayId} to the outlet.
     *
     * @throws WarehouseException if the SOAP call fails
     */
    public void requestPart(int trayId) throws WarehouseException {
        // TODO: warehouseService.pickItem(trayId)
        throw new UnsupportedOperationException("TODO: implement ProductionOrchestrator.requestPart");
    }

    /**
     * Commands the AGV to pick up the part from the warehouse and deliver it to the
     * assembly station.
     *
     * <p>AGV program sequence:
     * <ol>
     *   <li>{@link AgvProgram#MOVE_TO_STORAGE} + execute</li>
     *   <li>{@link AgvProgram#PICK_WAREHOUSE}  + execute</li>
     *   <li>{@link AgvProgram#MOVE_TO_ASSEMBLY}+ execute</li>
     *   <li>{@link AgvProgram#PUT_ASSEMBLY}    + execute</li>
     * </ol>
     *
     * @throws AgvException if any AGV REST call fails
     */
    public void transportToAssembly() throws AgvException {
        // TODO: For each step: agvService.loadProgram(program), agvService.executeProgram()
        //       Poll agvService.getStatus() until state == IDLE before proceeding to next step.
        throw new UnsupportedOperationException("TODO: implement ProductionOrchestrator.transportToAssembly");
    }

    /**
     * Publishes an assembly-start command to the assembly station.
     *
     * @param processId unique integer identifying this production run
     * @throws AssemblyStationException if the MQTT publish fails
     */
    public void startAssembly(int processId) throws AssemblyStationException {
        // TODO: assemblyStationService.startOperation(processId)
        throw new UnsupportedOperationException("TODO: implement ProductionOrchestrator.startAssembly");
    }

    /**
     * Commands the AGV to collect the assembled product from the assembly station
     * and deliver it back to the warehouse, then records it in the warehouse.
     *
     * <p>AGV program sequence:
     * <ol>
     *   <li>{@link AgvProgram#PICK_ASSEMBLY}   + execute</li>
     *   <li>{@link AgvProgram#MOVE_TO_STORAGE} + execute</li>
     *   <li>{@link AgvProgram#PUT_WAREHOUSE}   + execute</li>
     * </ol>
     *
     * @param trayId            destination tray in the warehouse
     * @param assembledItemName name to register for the stored product
     * @throws St4Exception if any AGV or warehouse call fails
     */
    public void returnToWarehouse(int trayId, String assembledItemName) throws St4Exception {
        // TODO:
        //  1. AGV: PICK_ASSEMBLY, MOVE_TO_STORAGE, PUT_WAREHOUSE (each: load + execute + wait idle)
        //  2. warehouseService.insertItem(trayId, assembledItemName)
        throw new UnsupportedOperationException("TODO: implement ProductionOrchestrator.returnToWarehouse");
    }
}
