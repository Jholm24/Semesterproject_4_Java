package dk.sdu.st4.common.services;

import java.util.List;
import java.util.Map;

public interface IAgvRegistry {
    void loadFromDb();
    void connectNext();
    void connect(String serialNo);
    void disconnect(String serialNo);
    List<Map<String, Object>> getMachinesStatus();
    IAgv acquire();
    void release(IAgv agv);
}
