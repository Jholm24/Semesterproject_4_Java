package dk.sdu.st4.common.Interfaces;

import java.util.concurrent.CompletableFuture;

public interface IConnect {
    int getMachineId();
    void setMachineId(int machineId);


    String getMachineType();
    void setMachineType(String machineType);

    void addMachine(int machineId, String machineType);
    void removeMachine(int machineId);
    CompletableFuture<Void> connectMachine(int machineId);
    void disconnectMachine(int machineId);
    boolean isConnected(int machineId);
    // Vi er fkn igang

}
