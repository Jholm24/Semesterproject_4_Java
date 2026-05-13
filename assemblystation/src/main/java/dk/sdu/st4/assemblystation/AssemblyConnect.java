package dk.sdu.st4.assemblystation;

import dk.sdu.st4.common.services.IConnect;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AssemblyConnect implements IConnect {

    private final AssemblyModel model;

    public AssemblyConnect() {
        this("localhost", 1883);
    }

    public AssemblyConnect(String broker, int port) {
        model = new AssemblyModel();
        model.broker = broker;
        model.port = port;
    }

    AssemblyModel getModel() { return model; }

    private MqttCallback buildCallback() {
        return new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("Connection lost: " + cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                switch (topic) {
                    case "emulator/operation":
                        JsonParser.parseString(payload).getAsJsonObject();
                        break;
                    case "emulator/status":
                        JsonObject jsonStatus = JsonParser.parseString(payload).getAsJsonObject();
                        model.state           = jsonStatus.get("State").getAsInt();
                        model.lastOperationId = jsonStatus.get("LastOperation").getAsInt();
                        model.operationId     = jsonStatus.get("CurrentOperation").getAsInt();
                        break;
                    case "emulator/checkhealth":
                        JsonObject jsonHealth = JsonParser.parseString(payload).getAsJsonObject();
                        model.isHealthy = jsonHealth.get("IsHealthy").getAsBoolean();
                        break;
                    case "emulator/response":
                        model.state = 0;
                        System.out.println("Assembly response: " + payload);
                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        };
    }

    @Override public String getMachineId()                                          { return model.serialNumber; }
    @Override public void   setMachineId(String serialNumber)                      { model.serialNumber = serialNumber; }
    @Override public String getMachineType()                                        { return model.machineType; }
    @Override public void   setMachineType(String type)                            { model.machineType = type; }
    @Override public void   addMachine(String sn, String type, String v, String p, String u) {}
    @Override public void   removeMachine(String serialNumber)                     {}

    @Override
    public CompletableFuture<Void> connectMachine(String machineId) {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setKeepAliveInterval(10);
            options.setAutomaticReconnect(true);

            model.mqttClient = new MqttClient(
                    "tcp://" + model.broker + ":" + model.port,
                    machineId,
                    new MemoryPersistence()
            );
            model.mqttClient.setCallback(buildCallback());
            model.mqttClient.connect(options);

            model.mqttClient.subscribe("emulator/status");
            model.mqttClient.subscribe("emulator/checkhealth");
            model.mqttClient.subscribe("emulator/operation");
            model.mqttClient.subscribe("emulator/response");

            System.out.println("Connected to " + model.broker + ":" + model.port);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void disconnectMachine(String serialNumber) {
        try {
            model.mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isConnected(String machineId) {
        return model.mqttClient != null && model.mqttClient.isConnected();
    }
}
