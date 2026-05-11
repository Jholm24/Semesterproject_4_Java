package dk.sdu.st4.assemblystation;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class AssemblyModel {
    // MQTT
    public String     broker;
    public int        port;
    public MqttClient mqttClient;

    // IAssembly — volatile: written by MQTT callback thread, read by orchestrator thread
    public volatile int state;
    public volatile boolean isHealthy = true;
    public volatile int operationId;
    public volatile int lastOperationId;

    // IConnect
    public String serialNumber;
    public String machineType;
}
