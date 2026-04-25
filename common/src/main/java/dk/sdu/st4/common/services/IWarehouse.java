package dk.sdu.st4.common.services;

public interface IWarehouse {

    // Methods
    void PickItem (int trayID , String machineID );
    void InsertItem (int trayID, String name , String machineID);
    void GetInventory (String machineID);
    int GetState(String machineID);
}