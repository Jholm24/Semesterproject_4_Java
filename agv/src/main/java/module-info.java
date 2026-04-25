/**
 * AGV module — REST client component.
 *
 * Communicates with the AGV via HTTP PUT/GET using the built-in {@code java.net.http} client.
 * Implements {@link dk.sdu.st4.common.services.IAgv}.
 *
 * REST endpoint: http://localhost:8082/v1/status/
 */
module dk.sdu.st4.agv {
    requires java.net.http;
    requires dk.sdu.st4.common;

    exports dk.sdu.st4.agv.service;
}
