package dk.sdu.st4.assemblystation;
import java.util.Random;
import dk.sdu.st4.common.services.IAssembly;
import dk.sdu.st4.common.services.IConnect;
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
    public AssemblyController(int machineUrl) throws MqttException {
        model = new AssemblyModel();
        model.broker = "localhost";
        model.port = machineUrl;
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

    // --- IAssembly getters/setters ---//
    @Override public void sendOperationId(int id) {

        String json = String.format("{\"ProcessID\": %d}", id);
        try {
            MqttMessage message = new MqttMessage(json.getBytes(StandardCharsets.UTF_8));
            mqttClient.publish("emulator/operation", message);
        } catch (MqttException e){
            e.printStackTrace();
        }
    }

    //Random operation number so we can show different operations
    @Override public void setExecuteOperation(){
        Random r = new Random();
        int number = r.nextInt(9998) + 1;
        sendOperationId(number);
    }

    @Override public void setErrorOperation(){

        sendOperationId(1);
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

    // --- IConnect getters/setters ---
    @Override public int getMachineId()               { return model.machineId; }
    @Override public void setMachineId(int machineId) { model.machineId = machineId; }

    @Override public String getMachineType()          { return model.machineType; }
    @Override public void setMachineType(String type) { model.machineType = type; }

    @Override
    public void addMachine(int machineId, String url, String machineType) {

    }

    // --- IConnect methods ---
    @Override
    public CompletableFuture<Void> connectMachine(int machineId) {
        return CompletableFuture.runAsync(() -> {
            try {
                MqttConnectOptions options = new MqttConnectOptions();

                options.setCleanSession(true);
                options.setKeepAliveInterval(60);
                options.setAutomaticReconnect(true);

                mqttClient = new MqttClient(
                        "tcp://" + model.broker + ":" + machineId,
                        UUID.randomUUID().toString(),
                        new MemoryPersistence()
                );
                mqttClient.setCallback(buildCallback());
                mqttClient.connect(options);
                subscribeAll();
                System.out.println("Connected to " + model.broker + ":" + machineId);
            } catch (MqttException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @Override public void removeMachine(int machineId) {}
    @Override public void disconnectMachine(int machineId) {
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    @Override public boolean isConnected(int machineId) { return mqttClient.isConnected(); }

}