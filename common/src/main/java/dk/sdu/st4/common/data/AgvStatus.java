package dk.sdu.st4.common.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import dk.sdu.st4.common.data.enums.AgvState;

public class AgvStatus {

    @JsonProperty("battery")
    private int battery;

    @JsonProperty("program name")
    private String programName;

    @JsonProperty("state")
    private AgvState state;

    @JsonProperty("timestamp")
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
