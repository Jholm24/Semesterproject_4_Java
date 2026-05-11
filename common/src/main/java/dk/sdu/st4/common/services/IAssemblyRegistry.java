package dk.sdu.st4.common.services;

import java.util.concurrent.ExecutionException;

public interface IAssemblyRegistry {
    void loadFromDb() throws Exception;
    void addMachine(String serialNumber, String type, String baseUrl);
    void removeMachine(String serialNumber);
    IConnect connectNext() throws ExecutionException, InterruptedException;
    IAssembly acquire();
    void release(IAssembly assembly);
}
