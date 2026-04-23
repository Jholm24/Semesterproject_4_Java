package dk.sdu.st4.common.config;

/**
 * Centralised configuration constants for all ST4 integration endpoints.
 *
 * <p>Values match the port mappings declared in {@code docker-compose.yml}:
 * <ul>
 *   <li>AGV    — REST  on port 8082</li>
 *   <li>Warehouse — SOAP on port 8087</li>
 *   <li>MQTT broker — port 1883 (TCP), 9001 (WebSocket)</li>
 * </ul>
 */
public final class AppConfig {

    // -------------------------------------------------------------------------
    // AGV REST API
    // -------------------------------------------------------------------------

    /** Base URL for all AGV REST calls. */
    public static final String AGV_BASE_URL = "http://localhost:8082/v1/status/";

    /** JSON "State" value used when loading a program onto the AGV. */
    public static final int AGV_LOAD_STATE = 1;

    /** JSON "State" value used when executing the loaded program. */
    public static final int AGV_EXECUTE_STATE = 2;

    // -------------------------------------------------------------------------
    // Warehouse SOAP API
    // -------------------------------------------------------------------------

    /** Service endpoint URL for Warehouse SOAP requests (also WSDL location). */
    public static final String WAREHOUSE_SERVICE_URL = "http://localhost:8087/Service.asmx";

    /** XML namespace used in Warehouse SOAP envelopes. */
    public static final String WAREHOUSE_SOAP_NAMESPACE = "http://tempuri.org/";

    // -------------------------------------------------------------------------
    // Assembly Station MQTT
    // -------------------------------------------------------------------------

    /** MQTT broker URL (TCP transport). */
    public static final String MQTT_BROKER_URL = "tcp://localhost:1883";

    /** MQTT broker URL (WebSocket transport — for browser-based clients). */
    public static final String MQTT_BROKER_WS_URL = "ws://localhost:9001";

    /** Topic to publish assembly-operation commands to. */
    public static final String MQTT_TOPIC_OPERATION = "emulator/operation";

    /** Topic to subscribe to for frequent Assembly Station heartbeat/state updates. */
    public static final String MQTT_TOPIC_STATUS = "emulator/status";

    /** Topic to subscribe to for quality-control results after assembly completes. */
    public static final String MQTT_TOPIC_HEALTH = "emulator/checkhealth";

    /**
     * Special ProcessID that triggers a deliberately unhealthy assembly result.
     * Use only for error-handling tests.
     */
    public static final int MQTT_UNHEALTHY_PROCESS_ID = 9999;

    private AppConfig() {
        // utility class — not instantiable
    }
}
