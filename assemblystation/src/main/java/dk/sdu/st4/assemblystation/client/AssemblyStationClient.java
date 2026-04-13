package dk.sdu.st4.assemblystation.client;

import dk.sdu.st4.core.exception.AssemblyStationException;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * Low-level Eclipse Paho MQTT v3 client wrapper for the Assembly Station.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Manage the MQTT connection lifecycle (connect / disconnect).</li>
 *   <li>Provide {@link #publish(String, String)} for sending commands.</li>
 *   <li>Provide {@link #subscribe(String, IMqttMessageListener)} for receiving messages.</li>
 * </ul>
 *
 * <p>Broker: {@code tcp://localhost:1883}
 */
public class AssemblyStationClient {

    private MqttClient mqttClient;
    private final String brokerUrl;
    private final String clientId;

    public AssemblyStationClient(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        this.clientId = MqttClient.generateClientId();
    }

    /**
     * Opens a TCP connection to the MQTT broker.
     * Must be called before {@link #publish} or {@link #subscribe}.
     *
     * @throws AssemblyStationException if the connection cannot be established
     */
    public void connect() throws AssemblyStationException {
        // TODO:
        //  1. Instantiate MqttClient: new MqttClient(brokerUrl, clientId)
        //  2. Configure MqttConnectOptions (e.g. setCleanSession(true), setConnectionTimeout(10))
        //  3. Call mqttClient.connect(options)
        //  4. Wrap MqttException in AssemblyStationException
        throw new UnsupportedOperationException("TODO: implement AssemblyStationClient.connect");
    }

    /**
     * Gracefully disconnects from the MQTT broker and releases resources.
     *
     * @throws AssemblyStationException if disconnection fails
     */
    public void disconnect() throws AssemblyStationException {
        // TODO:
        //  1. If mqttClient != null && mqttClient.isConnected(): mqttClient.disconnect()
        //  2. mqttClient.close()
        //  3. Wrap MqttException in AssemblyStationException
        throw new UnsupportedOperationException("TODO: implement AssemblyStationClient.disconnect");
    }

    /**
     * Publishes a UTF-8 encoded string message to the given topic at QoS 1.
     *
     * @param topic   MQTT topic string
     * @param payload message payload (typically a JSON string)
     * @throws AssemblyStationException if the publish fails or the client is not connected
     */
    public void publish(String topic, String payload) throws AssemblyStationException {
        // TODO:
        //  1. Create MqttMessage from payload.getBytes(StandardCharsets.UTF_8)
        //  2. Set QoS to 1: message.setQos(1)
        //  3. Call mqttClient.publish(topic, message)
        //  4. Wrap MqttException in AssemblyStationException
        throw new UnsupportedOperationException("TODO: implement AssemblyStationClient.publish");
    }

    /**
     * Subscribes to the given topic and registers a message listener at QoS 1.
     *
     * @param topic    MQTT topic string (supports wildcards, e.g. {@code emulator/#})
     * @param listener Paho callback invoked for each received message
     * @throws AssemblyStationException if the subscription fails or the client is not connected
     */
    public void subscribe(String topic, IMqttMessageListener listener) throws AssemblyStationException {
        // TODO:
        //  1. Call mqttClient.subscribe(topic, 1, listener)
        //  2. Wrap MqttException in AssemblyStationException
        throw new UnsupportedOperationException("TODO: implement AssemblyStationClient.subscribe");
    }
}
