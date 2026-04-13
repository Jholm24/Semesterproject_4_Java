package dk.sdu.st4.assemblystation.service;

import dk.sdu.st4.assemblystation.client.AssemblyStationClient;
import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.util.JsonUtil;
import dk.sdu.st4.core.exception.AssemblyStationException;
import dk.sdu.st4.core.model.AssemblyStatus;
import dk.sdu.st4.core.model.HealthCheckResult;
import dk.sdu.st4.core.service.IAssemblyStationService;

import java.util.function.Consumer;

/**
 * MQTT-backed implementation of {@link IAssemblyStationService}.
 *
 * <p>Wraps {@link AssemblyStationClient} to provide higher-level publish/subscribe
 * operations with typed domain objects.
 */
public class AssemblyStationServiceImpl implements IAssemblyStationService {

    private final AssemblyStationClient client;

    /** Creates a service instance targeting the default broker URL from {@link AppConfig}. */
    public AssemblyStationServiceImpl() throws AssemblyStationException {
        this(AppConfig.MQTT_BROKER_URL);
    }

    /**
     * Creates a service instance targeting a custom broker URL (useful for testing).
     * Immediately establishes the MQTT connection.
     */
    public AssemblyStationServiceImpl(String brokerUrl) throws AssemblyStationException {
        this.client = new AssemblyStationClient(brokerUrl);
        // TODO: call client.connect() here (connection established on construction)
    }

    /**
     * {@inheritDoc}
     *
     * <p>Publishes: {@code {"ProcessID": <processId>}} to {@code emulator/operation}.
     */
    @Override
    public void startOperation(int processId) throws AssemblyStationException {
        // TODO:
        //  1. Build JSON payload: {"ProcessID": processId}
        //     e.g. using JsonUtil.toJson(Map.of("ProcessID", processId))
        //     or by constructing the string directly.
        //  2. client.publish(AppConfig.MQTT_TOPIC_OPERATION, payload)
        throw new UnsupportedOperationException("TODO: implement AssemblyStationServiceImpl.startOperation");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Subscribes to {@code emulator/status}; each message is deserialised to
     * {@link AssemblyStatus} and forwarded to the provided {@code callback}.
     */
    @Override
    public void subscribeToStatus(Consumer<AssemblyStatus> callback) throws AssemblyStationException {
        // TODO:
        //  client.subscribe(AppConfig.MQTT_TOPIC_STATUS, (topic, message) -> {
        //      String json = new String(message.getPayload(), StandardCharsets.UTF_8);
        //      AssemblyStatus status = JsonUtil.fromJson(json, AssemblyStatus.class);
        //      callback.accept(status);
        //  });
        throw new UnsupportedOperationException("TODO: implement AssemblyStationServiceImpl.subscribeToStatus");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Subscribes to {@code emulator/checkhealth}; each message is deserialised to
     * {@link HealthCheckResult} and forwarded to the provided {@code callback}.
     */
    @Override
    public void subscribeToHealthCheck(Consumer<HealthCheckResult> callback) throws AssemblyStationException {
        // TODO:
        //  client.subscribe(AppConfig.MQTT_TOPIC_HEALTH, (topic, message) -> {
        //      String json = new String(message.getPayload(), StandardCharsets.UTF_8);
        //      HealthCheckResult result = JsonUtil.fromJson(json, HealthCheckResult.class);
        //      callback.accept(result);
        //  });
        throw new UnsupportedOperationException("TODO: implement AssemblyStationServiceImpl.subscribeToHealthCheck");
    }

    /** {@inheritDoc} */
    @Override
    public void disconnect() throws AssemblyStationException {
        // TODO: client.disconnect()
        throw new UnsupportedOperationException("TODO: implement AssemblyStationServiceImpl.disconnect");
    }
}
