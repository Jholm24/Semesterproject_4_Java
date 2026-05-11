package dk.sdu.st4.common.services;

public interface IAgvRegistry {
    void loadFromDb();
    void connectNext();
    IAgv acquire();
    void release(IAgv agv);
}
