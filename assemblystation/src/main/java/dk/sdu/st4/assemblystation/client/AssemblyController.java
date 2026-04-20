package dk.sdu.st4.assemblystation.client;
import java.util.Random;
import dk.sdu.st4.common.Interfaces.IAssembly;
import dk.sdu.st4.common.Interfaces.IConnect;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
                        JsonObject jsonOperation = JsonParser.parseString(payload).getAsJsonObject();
                        break;
                    case "emulator/status":
                        JsonObject jsonStatus = JsonParser.parseString(payload).getAsJsonObject();
                        model.state = jsonStatus.get("State").getAsInt();
                        model.lastOperationId = jsonStatus.get("LastOperation").getAsInt();
                        model.operationId = jsonStatus.get("CurrentOperation").getAsInt();
                        System.out.println("State: " + model.state);
                        System.out.println("CurrentOperation: " + model.operationId);
                        System.out.println("LastOperation: " + model.lastOperationId);
                        break;
                    case "emulator/checkhealth":
                        JsonObject jsonHealth = JsonParser.parseString(payload).getAsJsonObject();
                        model.isHealthy = jsonHealth.get("IsHealthy").getAsBoolean();
                        break;
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        };
    }

    // --- IAssembly getters/setters ---
    @Override public int getState()                  {
        return model.state;
    }
    @Override public int getOperationId()            {
        return model.operationId;
    }
    @Override public boolean isHealthy()             {
        return model.isHealthy;
    }
    @Override public void setState(int state)        { model.state = state; }

    @Override public void setHealthy(boolean h)      { model.isHealthy = h; }

    @Override public void setOperationId(int id) {

        String json = String.format("{\"ProcessID\": %s}", id);
        try {
            MqttMessage message = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
            mqttClient.publish("emulator/operation", message);
        } catch (MqttException e){
            e.printStackTrace();
        }
    }

    //Random operation number so we can show different operations
    @Override public void executeOperation(){
        Random r = new Random();
        int number = r.nextInt(9998) + 1;
        setOperationId(number);
    }

    @Override public void errorOperation(){
        setOperationId(9999);
    }

    @Override public int getLastOperationId()        { return model.lastOperationId; }

    @Override
    public int getStatus() throws MqttException, InterruptedException {
        return model.state;
    }

    @Override public boolean getHealth() throws MqttException, InterruptedException{
        return model.isHealthy;
    }
    @Override public int getOperation() throws MqttException, InterruptedException{
        return model.operationId;
    }

    @Override
    public void subscribeAll() throws MqttException, InterruptedException {
        mqttClient.subscribe("emulator/status");
        mqttClient.subscribe("emulator/checkhealth");
        mqttClient.subscribe("emulator/operation");
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

}