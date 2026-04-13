package dk.sdu.st4.core.model;

/**
 * Quality-control result published by the Assembly Station on MQTT topic
 * {@code emulator/checkhealth} after an assembly process completes.
 *
 * A result is always healthy for simulated processes unless ProcessID {@code 9999}
 * was used to trigger a deliberate unhealthy outcome.
 */
public class HealthCheckResult {

    /** {@code true} if the assembled product passed quality control. */
    private boolean healthy;

    /** Process ID of the assembly operation this result belongs to. */
    private int processId;

    /** Timestamp of the health-check publication. */
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
