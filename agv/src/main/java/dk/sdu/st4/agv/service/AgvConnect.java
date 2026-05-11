package dk.sdu.st4.agv.service;

import dk.sdu.st4.agv.client.AgvClient;
import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.services.IConnect;

import java.util.concurrent.CompletableFuture;

public class AgvConnect implements IConnect {

    private final AgvModel model;

    public AgvConnect() {
        this(AppConfig.AGV_BASE_URL);
    }

    public AgvConnect(String baseUrl) {
        model = new AgvModel();
        model.baseUrl = baseUrl;
    }

    AgvModel getModel() { return model; }

    @Override public String getMachineId()                                          { return model.serialNumber; }
    @Override public void   setMachineId(String serialNumber)                      { model.serialNumber = serialNumber; }
    @Override public String getMachineType()                                        { return model.machineType; }
    @Override public void   setMachineType(String type)                            { model.machineType = type; }
    @Override public void   addMachine(String sn, String type, String v, String u) {}
    @Override public void   removeMachine(String serialNumber)                     {}

    @Override
    public CompletableFuture<Void> connectMachine(String serialNumber) {
        model.client       = new AgvClient(model.baseUrl);
        model.serialNumber = serialNumber;
        System.out.println("Connected AGV at " + model.baseUrl);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void disconnectMachine(String serialNumber) {
        model.client = null;
    }

    @Override
    public boolean isConnected(String serialNumber) { return model.client != null; }
}
