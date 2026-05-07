/**
 * AGV module — REST client component.
 * Implements {@link dk.sdu.st4.common.services.IAgv}.
**/

module dk.sdu.st4.agv {
    requires java.net.http;
    requires dk.sdu.st4.common;

    exports dk.sdu.st4.agv.service;
}
