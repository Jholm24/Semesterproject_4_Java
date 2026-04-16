package dk.sdu.st4.assemblystation.client;
public class AssemblyModel {
    // MQTT
    public String broker;
    public int port;

    // IAssembly
    public int state;
    public boolean isHealthy;
    public String operationId;
    public String lastOperationId;

    // IConnect
    public int machineId;
    public String machineType;
}
