package dk.sdu.st4.assemblystation;

import dk.sdu.st4.common.services.IAssembly;
import org.eclipse.paho.client.mqttv3.MqttException;

public class AssemblyServiceImpl implements IAssembly {

    private final AssemblyController controller;

    public AssemblyServiceImpl(String broker, int port) throws MqttException {
        this.controller = new AssemblyController(broker, port);
    }

    public void connect(String serialNo) {
        controller.connectMachine(serialNo);
    }

    public void disconnect(String serialNo) {
        controller.disconnectMachine(serialNo);
    }

    @Override public void sendOperationId(int operationId) { controller.sendOperationId(operationId); }
    @Override public int  getLastOperationId()             { return controller.getLastOperationId(); }
    @Override public int  getStatus() throws Exception     { return controller.getStatus(); }
    @Override public boolean getHealth() throws Exception  { return controller.getHealth(); }
    @Override public int  getOperation() throws Exception  { return controller.getOperation(); }
    @Override public void subscribeAll() throws Exception  { controller.subscribeAll(); }
    @Override public void setExecuteOperation()            { controller.setExecuteOperation(); }
    @Override public void setErrorOperation()              { controller.setErrorOperation(); }
}
