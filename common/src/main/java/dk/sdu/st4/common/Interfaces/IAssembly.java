package dk.sdu.st4.common.Interfaces;
public interface IAssembly {
    int getState();
    void setState(int state);

    boolean isHealthy();
    void setHealthy(boolean isHealthy);

    String getOperationId();
    void setOperationId(String operationId);

    String getLastOperationId();
    void setLastOperationId (String lastOperationId)  ;

    int getStatus() throws Exception;
    boolean getHealth() throws Exception;
    String getOperation() throws Exception;
    String getLastOperation();
    void subscribeAll() throws Exception;
    void executeOperation();
    void errorOperation();
}