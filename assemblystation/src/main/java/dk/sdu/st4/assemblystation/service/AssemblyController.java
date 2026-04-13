package dk.sdu.st4.assemblystation.service;

import dk.sdu.st4.common.Interfaces.IAssembly;
import dk.sdu.st4.common.Interfaces.IConnect;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AssemblyController implements IConnect, IAssembly {

    private final AssemblyModel model;
    private MqttClient mqttClient;

    public AssemblyController() throws MqttException {
        model = new AssemblyModel();
        model.broker = "localhost";
        model.port = 1883;

        mqttClient = new MqttClient(
                "tcp://" + model.broker + ":" + model.port,
                UUID.randomUUID().toString(),
                new MemoryPersistence()
        );
        mqttClient.setCallback(buildCallback());
    }

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
                        model.operationId = Integer.parseInt(payload);
                        System.out.println("Operation: " + payload);
                        break;
                    case "emulator/status":
                        model.isHealthy = payload.equals("healthy");
                        System.out.println("Status: " + payload);
                        break;
                    case "emulator/checkhealth":
                        System.out.println("Health: " + payload);
                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        };
    }

    // --- IAssembly getters/setters ---
    @Override public int getState()                  { return model.state; }
    @Override public void setState(int state)        { model.state = state; }

    @Override public boolean isHealthy()             { return model.isHealthy; }
    @Override public void setHealthy(boolean h)      { model.isHealthy = h; }

    @Override public int getOperationId()            { return model.operationId; }
    @Override public void setOperationId(int id)     { model.operationId = id; }

    @Override public int getLastOperationId()        { return model.lastOperationId; }
    @Override public void setLastOperationId(int id) { model.lastOperationId = id; }

    @Override
    public int getStatus() throws MqttException, InterruptedException {
        mqttClient.subscribe("emulator/status");
        return model.state;
    }

    @Override public boolean getHealth() throws MqttException, InterruptedException{
        mqttClient.subscribe("emulator/checkhealth");
        return model.isHealthy;
    }
    @Override public int getOperation() throws MqttException, InterruptedException{
        mqttClient.subscribe("emulator/operation");
        return model.operationId;
    }

    @Override public void subscribeAll() throws MqttException, InterruptedException{
        getStatus();
        getHealth();
        getOperation();
    }
    @Override public int getLastOperation() { return model.lastOperationId; }

    // --- IConnect getters/setters ---
    @Override public int getMachineId()               { return model.machineId; }
    @Override public void setMachineId(int machineId) { model.machineId = machineId; }

    @Override public String getMachineType()          { return model.machineType; }
    @Override public void setMachineType(String type) { model.machineType = type; }

    // --- IConnect methods ---
    @Override
    public CompletableFuture<Void> connectMachine(int machineId) {
        return CompletableFuture.runAsync(() -> {
            try {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);

                mqttClient = new MqttClient(
                        "tcp://" + model.broker + ":" + machineId,
                        UUID.randomUUID().toString(),
                        new MemoryPersistence()
                );
                mqttClient.setCallback(buildCallback());
                mqttClient.connect(options);
                System.out.println("Connected to " + model.broker + ":" + machineId);
            } catch (MqttException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override public void addMachine(int machineId, String machineType) {}
    @Override public void removeMachine(int machineId) {}
    @Override public void disconnectMachine(int machineId) {}
    @Override public boolean isConnected(int machineId) { return mqttClient.isConnected(); }

    public void sendCommand(String command) throws MqttException {
        MqttMessage message = new MqttMessage(command.getBytes(StandardCharsets.UTF_8));
        mqttClient.publish("assembly/" + model.machineId + "/command", message);
    }

}