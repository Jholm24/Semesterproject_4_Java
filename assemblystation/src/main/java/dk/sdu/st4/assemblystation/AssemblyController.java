package dk.sdu.st4.assemblystation;

import dk.sdu.st4.common.services.IAssembly;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class AssemblyController implements IAssembly {

    private final AssemblyModel model;

    public AssemblyController() {
        this(new AssemblyModel());
    }

    public AssemblyController(AssemblyModel model) {
        this.model = model;
    }

    @Override
    public void sendOperationId(int id) {
        String json = String.format("{\"ProcessID\": %d}", id);
        try {
            long deadline = System.currentTimeMillis() + 15_000;
            while (!model.mqttClient.isConnected() && System.currentTimeMillis() < deadline) {
                Thread.sleep(200);
            }
            model.mqttClient.publish("emulator/operation",
                    new MqttMessage(json.getBytes(StandardCharsets.UTF_8)));
        } catch (MqttException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setExecuteOperation() {
        sendOperationId(new Random().nextInt(9998) + 1);
    }

    @Override
    public void setErrorOperation() {
        sendOperationId(1);
    }

    @Override public int     getLastOperationId()                { return model.lastOperationId; }
    @Override public int     getStatus()  throws MqttException   { return model.state; }
    @Override public boolean getHealth()  throws MqttException   { return model.isHealthy; }
    @Override public int     getOperation() throws MqttException { return model.operationId; }

    @Override
    public void subscribeAll() throws MqttException {
        model.mqttClient.subscribe("emulator/status");
        model.mqttClient.subscribe("emulator/checkhealth");
        model.mqttClient.subscribe("emulator/operation");
        model.mqttClient.subscribe("emulator/response");
    }
}
