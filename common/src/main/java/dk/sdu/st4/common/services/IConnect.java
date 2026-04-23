package dk.sdu.st4.common.services;

import java.util.concurrent.CompletableFuture;

public interface IConnect {
    int getMachineId();
    void setMachineId(int machineId);

    String getMachineType();
    void setMachineType(String machineType);

    void addMachine(int machineSerialNumber,String type, String variant, String base_url);
    void removeMachine(int machineSerialNumber);
    CompletableFuture<Void> connectMachine(int machineId);
    void disconnectMachine(int machineId);
    boolean isConnected(int machineId);
    // Vi er fkn igang
}