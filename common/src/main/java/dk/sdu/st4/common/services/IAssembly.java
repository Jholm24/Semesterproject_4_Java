package dk.sdu.st4.common.services;
public interface IAssembly {
    int getState();
    void setState(int state);

    boolean isHealthy();
    void setHealthy(boolean isHealthy);

    int getOperationId();
    void setOperationId(int operationId);

    int getLastOperationId();

    int getStatus() throws Exception;
    boolean getHealth() throws Exception;
    int getOperation() throws Exception;
    int getLastOperation();
    void subscribeAll() throws Exception;
    void executeOperation();
    void errorOperation();
}