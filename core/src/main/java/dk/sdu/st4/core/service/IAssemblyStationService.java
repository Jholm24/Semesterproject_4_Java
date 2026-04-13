package dk.sdu.st4.core.service;

import dk.sdu.st4.core.exception.AssemblyStationException;
import dk.sdu.st4.core.model.AssemblyStatus;
import dk.sdu.st4.core.model.HealthCheckResult;

import java.util.function.Consumer;

/**
 * Contract for communicating with the Assembly Station via MQTT.
 *
 * <p>MQTT broker: {@code tcp://localhost:1883}
 *
 * <p>Topics:
 * <ul>
 *   <li>{@code emulator/operation}  — publish commands (start assembly)</li>
 *   <li>{@code emulator/status}     — subscribe for frequent heartbeat/state updates</li>
 *   <li>{@code emulator/checkhealth}— subscribe for quality-control results</li>
 * </ul>
 */
public interface IAssemblyStationService {

    /**
     * Publishes a start-assembly command to {@code emulator/operation}.
     * The payload is {@code {"ProcessID": <processId>}}.
     *
     * <p>Use ProcessID {@code 9999} to deliberately trigger an unhealthy assembly result
     * (for error-handling testing).
     *
     * @param processId unique integer identifying this production run
     * @throws AssemblyStationException if publishing fails
     */
    void startOperation(int processId) throws AssemblyStationException;

    /**
     * Registers a callback that is invoked for every message received on
     * {@code emulator/status}. Suitable for connection heartbeating and
     * live monitoring of station state.
     *
     * @param callback consumer that receives each deserialized {@link AssemblyStatus}
     * @throws AssemblyStationException if the subscription cannot be established
     */
    void subscribeToStatus(Consumer<AssemblyStatus> callback) throws AssemblyStationException;

    /**
     * Registers a callback that is invoked when a quality-control result is published
     * on {@code emulator/checkhealth} after an assembly process completes.
     *
     * @param callback consumer that receives each deserialized {@link HealthCheckResult}
     * @throws AssemblyStationException if the subscription cannot be established
     */
    void subscribeToHealthCheck(Consumer<HealthCheckResult> callback) throws AssemblyStationException;

    /**
     * Gracefully disconnects the MQTT client from the broker.
     *
     * @throws AssemblyStationException if the disconnection fails
     */
    void disconnect() throws AssemblyStationException;
}
