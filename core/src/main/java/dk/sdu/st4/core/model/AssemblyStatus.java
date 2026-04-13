package dk.sdu.st4.core.model;

import dk.sdu.st4.core.enums.AssemblyState;

/**
 * Status broadcast received from the Assembly Station over MQTT topic {@code emulator/status}.
 *
 * <pre>
 * {
 *   "LastOperation":    1234,
 *   "CurrentOperation": 2345,
 *   "State":            1,
 *   "TimeStamp":        "12:34:56"
 * }
 * </pre>
 */
public class AssemblyStatus {

    /** Process ID of the last completed assembly operation. */
    private int lastOperation;

    /** Process ID of the assembly operation currently in progress (0 when idle). */
    private int currentOperation;

    /** Current operational state of the assembly station. */
    private AssemblyState state;

    /** Timestamp of the status message as reported by the assembly station. */
    private String timestamp;

    public AssemblyStatus() {}

    public AssemblyStatus(int lastOperation, int currentOperation, AssemblyState state, String timestamp) {
        this.lastOperation = lastOperation;
        this.currentOperation = currentOperation;
        this.state = state;
        this.timestamp = timestamp;
    }

    public int getLastOperation() { return lastOperation; }
    public void setLastOperation(int lastOperation) { this.lastOperation = lastOperation; }

    public int getCurrentOperation() { return currentOperation; }
    public void setCurrentOperation(int currentOperation) { this.currentOperation = currentOperation; }

    public AssemblyState getState() { return state; }
    public void setState(AssemblyState state) { this.state = state; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "AssemblyStatus{lastOperation=" + lastOperation + ", currentOperation="
                + currentOperation + ", state=" + state + ", timestamp='" + timestamp + "'}";
    }
}
