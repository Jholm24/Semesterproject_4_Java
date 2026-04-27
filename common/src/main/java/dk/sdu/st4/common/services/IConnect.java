package dk.sdu.st4.common.services;

import java.util.concurrent.CompletableFuture;

public interface IConnect {

    // Serial number
    String getMachineId();
    void setMachineId(String serialNumber);

    // Machine type
    String getMachineType();
    void setMachineType(String machineType);

    // Machine management
    void addMachine(String serialNumber,String type, String variant, String base_url);
    void removeMachine(String serialNumber);
    CompletableFuture<Void> connectMachine(String serialNumber);
    void disconnectMachine(String serialNumber);
    boolean isConnected(String serialNumber);
    // Vi er fkn igang
}