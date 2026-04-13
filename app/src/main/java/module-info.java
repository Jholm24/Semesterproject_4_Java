/**
 * App module — production orchestrator and application entry point.
 *
 * Wires all component modules together and drives the production sequence:
 *   Warehouse (SOAP) ↔ AGV (REST) ↔ Assembly Station (MQTT)
 *
 * Run with:
 *   java --module-path <all-jars> --module dk.sdu.st4.app/dk.sdu.st4.app.Main
 */
module dk.sdu.st4.app {
    requires dk.sdu.st4.core;
    requires dk.sdu.st4.agv;
    requires dk.sdu.st4.warehouse;
    requires dk.sdu.st4.assemblystation;
}
