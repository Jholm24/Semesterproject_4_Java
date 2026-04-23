package dk.sdu.st4.common.data;

import dk.sdu.st4.common.data.enums.AssemblyState;

public class AssemblyStatus {

    private int lastOperation;
    private int currentOperation;
    private AssemblyState state;
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
