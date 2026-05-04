package dk.sdu.st4.assemblystation;
public class AssemblyModel {
    // MQTT
    public String broker;
    public int port;

    // IAssembly — volatile: written by MQTT callback thread, read by orchestrator thread
    public volatile int state;
    public volatile boolean isHealthy;
    public volatile int operationId;
    public volatile int lastOperationId;

    // IConnect
    public String serialNumber;
    public String machineType;
}
