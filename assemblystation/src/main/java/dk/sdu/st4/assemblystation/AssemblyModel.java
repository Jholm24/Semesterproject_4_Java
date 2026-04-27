package dk.sdu.st4.assemblystation;
public class AssemblyModel {
    // MQTT
    public String broker;
    public int port;

    // IAssembly
    public int state;
    public boolean isHealthy;
    public int operationId;
    public int lastOperationId;

    // IConnect
    public String serialNumber;
    public String machineType;
}
