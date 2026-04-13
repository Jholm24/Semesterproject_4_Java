/**
 * AGV module — REST client component.
 * <p>
 * Communicates with the AGV via HTTP PUT/GET using the built-in {@code java.net.http} client.
 * Implements {@link dk.sdu.st4.core.service.IAgvService}.
 * <p>
 * REST endpoint: http://localhost:8082/v1/status/
 */
module dk.sdu.st4.agv {
    requires dk.sdu.st4.core;
    requires java.net.http;

    exports dk.sdu.st4.agv.service;
}
