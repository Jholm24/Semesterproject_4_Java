package dk.sdu.st4.common.data;

public class HealthCheckResult {

    private boolean healthy;
    private int processId;
    private String timestamp;

    public HealthCheckResult() {}

    public HealthCheckResult(boolean healthy, int processId, String timestamp) {
        this.healthy = healthy;
        this.processId = processId;
        this.timestamp = timestamp;
    }

    public boolean isHealthy() { return healthy; }
    public void setHealthy(boolean healthy) { this.healthy = healthy; }

    public int getProcessId() { return processId; }
    public void setProcessId(int processId) { this.processId = processId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "HealthCheckResult{healthy=" + healthy + ", processId=" + processId
                + ", timestamp='" + timestamp + "'}";
    }
}
