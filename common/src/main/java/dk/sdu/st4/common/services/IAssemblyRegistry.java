package dk.sdu.st4.common.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface IAssemblyRegistry {
    void loadFromDb() throws Exception;
    void addMachine(String serialNumber, String type, String baseUrl);
    void removeMachine(String serialNumber);
    IConnect connectNext() throws ExecutionException, InterruptedException;
    void connect(String serialNo) throws Exception;
    void disconnect(String serialNo);
    List<Map<String, Object>> getMachinesStatus();
    IAssembly acquire();
    void release(IAssembly assembly);
}
