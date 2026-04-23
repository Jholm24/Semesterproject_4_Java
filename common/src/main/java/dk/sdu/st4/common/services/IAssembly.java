package dk.sdu.st4.common.services;
public interface IAssembly {

    void sendOperationId(int operationId);
    int getLastOperationId();
    int getStatus() throws Exception;
    boolean getHealth() throws Exception;
    int getOperation() throws Exception;
    void subscribeAll() throws Exception;
    void setExecuteOperation();
    void setErrorOperation();
}