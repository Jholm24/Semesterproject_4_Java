package dk.sdu.st4.warehouse.service;

import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.services.IConnect;
import jakarta.xml.ws.BindingProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class WarehouseConnect implements IConnect {

    private final WarehouseModel model;

    public WarehouseConnect() {
        this(AppConfig.WAREHOUSE_SERVICE_URL);
    }

    public WarehouseConnect(String baseUrl) {
        model = new WarehouseModel();
        model.baseUrl = baseUrl;
    }

    public WarehouseModel getModel() { return model; }

    @Override public String getMachineId()                                          { return model.serialNumber; }
    @Override public void   setMachineId(String serialNumber)                      { model.serialNumber = serialNumber; }
    @Override public String getMachineType()                                        { return model.machineType; }
    @Override public void   setMachineType(String type)                            { model.machineType = type; }
    @Override public void   addMachine(String sn, String type, String v, String u) {}
    @Override public void   removeMachine(String serialNumber)                     {}

    @Override
    public CompletableFuture<Void> connectMachine(String serialNumber) {
        try {
            IEmulatorService_Service factory = new IEmulatorService_Service(
                    new URL(model.baseUrl + "?wsdl"),
                    IEmulatorService_Service.SERVICE
            );
            IEmulatorService svc = factory.getBasicHttpBindingIEmulatorService();
            ((BindingProvider) svc).getRequestContext()
                    .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, model.baseUrl);
            model.proxy        = svc;
            model.serialNumber = serialNumber;
            System.out.println("Connected to warehouse at " + model.baseUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid warehouse URL: " + model.baseUrl, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void disconnectMachine(String serialNumber) {
        model.proxy = null;
    }

    @Override
    public boolean isConnected(String serialNumber) { return model.proxy != null; }
}
