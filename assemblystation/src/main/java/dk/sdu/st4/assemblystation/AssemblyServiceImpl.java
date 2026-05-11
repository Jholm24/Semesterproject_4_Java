package dk.sdu.st4.assemblystation;

import dk.sdu.st4.common.services.IAssembly;

public class AssemblyServiceImpl implements IAssembly {

    private final AssemblyConnect    connect;
    private final AssemblyController controller;

    public AssemblyServiceImpl(String broker, int port) {
        this.connect    = new AssemblyConnect(broker, port);
        this.controller = new AssemblyController(connect.getModel());
    }

    public void connect(String serialNo)    { connect.connectMachine(serialNo); }
    public void disconnect(String serialNo) { connect.disconnectMachine(serialNo); }

    @Override public void    sendOperationId(int id)          { controller.sendOperationId(id); }
    @Override public int     getLastOperationId()             { return controller.getLastOperationId(); }
    @Override public int     getStatus()  throws Exception    { return controller.getStatus(); }
    @Override public boolean getHealth()  throws Exception    { return controller.getHealth(); }
    @Override public int     getOperation() throws Exception  { return controller.getOperation(); }
    @Override public void    subscribeAll() throws Exception  { controller.subscribeAll(); }
    @Override public void    setExecuteOperation()            { controller.setExecuteOperation(); }
    @Override public void    setErrorOperation()              { controller.setErrorOperation(); }
}
