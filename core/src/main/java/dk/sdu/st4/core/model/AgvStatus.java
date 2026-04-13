package dk.sdu.st4.core.model;

import dk.sdu.st4.core.enums.AgvState;

/**
 * Status snapshot returned by the AGV REST API.
 *
 * Maps to the JSON response of both GET and PUT requests:
 * <pre>
 * {
 *   "Battery":      42,
 *   "Program name": "MoveToAssemblyOperation",
 *   "State":        2,
 *   "TimeStamp":    "12:34:56"
 * }
 * </pre>
 */
public class AgvStatus {

    /** Battery level in percent (0–100). */
    private int battery;

    /** Name of the currently loaded (or last executed) program. */
    private String programName;

    /** Current operational state of the AGV. */
    private AgvState state;

    /** Timestamp of the status snapshot as reported by the AGV. */
    private String timestamp;

    public AgvStatus() {}

    public AgvStatus(int battery, String programName, AgvState state, String timestamp) {
        this.battery = battery;
        this.programName = programName;
        this.state = state;
        this.timestamp = timestamp;
    }

    public int getBattery() { return battery; }
    public void setBattery(int battery) { this.battery = battery; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public AgvState getState() { return state; }
    public void setState(AgvState state) { this.state = state; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "AgvStatus{battery=" + battery + ", programName='" + programName
                + "', state=" + state + ", timestamp='" + timestamp + "'}";
    }
}
