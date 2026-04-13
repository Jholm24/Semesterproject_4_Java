/**
 * Assembly Station module — MQTT client component.
 *
 * Communicates with the Assembly Station emulator via Eclipse Paho MQTT v3.
 * Implements {@link dk.sdu.st4.core.service.IAssemblyStationService}.
 *
 * MQTT broker: tcp://localhost:1883 (WebSocket: ws://localhost:9001)
 *
 * Topics:
 *   emulator/operation   — publish (start assembly)
 *   emulator/status      — subscribe (heartbeat / state)
 *   emulator/checkhealth — subscribe (quality-control results)
 *
 * Note: org.eclipse.paho.client.mqttv3 is an automatic JPMS module
 * (no module-info.java in the jar). Its automatic module name is derived
 * from the jar filename: org.eclipse.paho.client.mqttv3
 */
module dk.sdu.st4.assemblystation {
    requires dk.sdu.st4.core;
    requires dk.sdu.st4.common;
    requires org.eclipse.paho.client.mqttv3;

    exports dk.sdu.st4.assemblystation.service;
}
