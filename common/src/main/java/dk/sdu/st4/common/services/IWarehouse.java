package dk.sdu.st4.common.services;

public interface IWarehouse {

    // Methods
    void PickItem (int trayID);
    void InsertItem (int trayID, String name);
    void GetInventory ();
    int GetState();
}
