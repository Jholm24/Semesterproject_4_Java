package dk.sdu.st4.common.services;

public interface IConnect {
    String getMachineId();
    void setMachineId(String serial_no);

    String getMachineType();
    void setMachineType(String machineType);

    void addMachine(String serial_no,String type, String variant, String base_url);
    void removeMachine(String serial_no);
    void connectMachine(String serial_no);
    void disconnectMachine(String serial_no);
    boolean isConnected(String serial_no);
    // Vi er fkn igang
}