package dk.sdu.st4.warehouse.service;

import dk.sdu.st4.common.services.IConnect;

import java.util.concurrent.CompletableFuture;

public class WarehouseConnect implements IConnect {

    @Override
    public String getMachineId() {
        return "";
    }

    @Override
    public void setMachineId(String serialNumber) {

    }

    @Override
    public String getMachineType() {
        return "";
    }

    @Override
    public void setMachineType(String machineType) {

    }

    @Override
    public void addMachine(String serialNumber, String type, String variant, String base_url) {

    }

    @Override
    public void removeMachine(String serialNumber) {

    }

    @Override
    public CompletableFuture<Void> connectMachine(String serialNumber) {
        return null;
    }

    @Override
    public void disconnectMachine(String serialNumber) {

    }

    @Override
    public boolean isConnected(String serialNumber) {
        return false;
    }
}
