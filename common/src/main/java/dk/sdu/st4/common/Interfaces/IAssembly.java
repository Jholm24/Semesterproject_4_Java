package dk.sdu.st4.common.Interfaces;
public interface IAssembly {
    int getState();
    void setState(int state);

    boolean isHealthy();
    void setHealthy(boolean isHealthy);

    int getOperationId();
    void setOperationId(int operationId);

    int getLastOperationId();
    void setLastOperationId(int lastOperationId);

    int getStatus() throws Exception;
    boolean getHealth() throws Exception;
    int getOperation() throws Exception;
    int getLastOperation();
    void subscribeAll() throws Exception;
}