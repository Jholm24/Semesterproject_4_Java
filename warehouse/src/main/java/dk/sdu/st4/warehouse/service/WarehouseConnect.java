package dk.sdu.st4.warehouse.service;

import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IConnect;
import jakarta.xml.ws.BindingProvider;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WarehouseConnect implements IConnect {

    private String machineId;
    private String machineType;

    final Map<String, IEmulatorService> connections = new HashMap<>();
    private final Map<String, Boolean> connectionState = new HashMap<>();

    public WarehouseConnect() {}

    @Override
    public String getMachineId() {
        return machineId;
    }

    @Override
    public void setMachineId(String serialNumber) {
        this.machineId = serialNumber;
    }

    @Override
    public String getMachineType() {
        return machineType;
    }

    @Override
    public void setMachineType(String type) {
        this.machineType = type;
    }

    @Override
    public void addMachine(String serialNumber, String type, String variant, String base_url) {
        String sql = """
                INSERT INTO machines (serial_no, type, variant, base_url)
                VALUES (?, ?, ?, ?);
                """;
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, serialNumber);
            statement.setString(2, type);
            statement.setString(3, variant);
            statement.setString(4, base_url);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMachine(String serialNumber) {
        String sql = "DELETE FROM machines WHERE serial_no = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serialNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connections.remove(serialNumber);
        connectionState.remove(serialNumber);
    }

    @Override
    public CompletableFuture<Void> connectMachine(String serialNumber) {
        return CompletableFuture.runAsync(() -> {
            String sql = "SELECT base_url FROM machines WHERE serial_no = ?";
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, serialNumber);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) throw new RuntimeException("Ingen maskine fundet med id: " + serialNumber);
                String url = rs.getString("base_url");

                IEmulatorService_Service factory = new IEmulatorService_Service(
                        new URL(url + "?wsdl"),
                        IEmulatorService_Service.SERVICE
                );
                IEmulatorService warehouseService = factory.getBasicHttpBindingIEmulatorService();
                ((BindingProvider) warehouseService).getRequestContext()
                        .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
                connections.put(serialNumber, warehouseService);
                connectionState.put(serialNumber, true);
            } catch (Exception e) {
                throw new RuntimeException("Failed to connect to warehouse " + serialNumber, e);
            }
        });
    }

    @Override
    public void disconnectMachine(String serialNumber) {
        connections.remove(serialNumber);
        connectionState.put(serialNumber, false);
    }

    @Override
    public boolean isConnected(String serialNumber) {
        return connectionState.getOrDefault(serialNumber, false);
    }

    IEmulatorService getService(String serialNumber) {
        return connections.get(serialNumber);
    }
}
