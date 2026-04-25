package dk.sdu.st4.common.services;

import java.util.concurrent.CompletableFuture;

public interface IConnect {

    // Serial number
    String getMachineId();
    void setMachineId(String machineId);

    // Machine type
    String getMachineType();
    void setMachineType(String machineType);

    // Machine management
    void addMachine(String machineSerialNumber,String type, String variant, String base_url);
    void removeMachine(String machineSerialNumber);
    CompletableFuture<Void> connectMachine(String machineId);
    void disconnectMachine(String machineId);
    boolean isConnected(String machineId);
    // Vi er fkn igang
}