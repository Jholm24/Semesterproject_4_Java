package dk.sdu.st4.common.Interfaces;

public interface IWarehouse {

    // Methods
    void PickItem (int trayID , int machineID );
    void InsertItem (int trayID, String name , int machineID);
    void GetInventory (int machineID);
    int GetState(int machineID);
}
