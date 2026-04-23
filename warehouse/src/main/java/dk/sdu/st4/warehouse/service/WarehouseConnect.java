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

    private int machineId;
    private String machineType;

    private final Map<Integer, IEmulatorService> connections = new HashMap<>();
    private final Map<Integer, Boolean> connectionState = new HashMap<>();

    public IEmulatorService getConnection(int machineSerialNumber) {
        return connections.get(machineSerialNumber);
    }

    @Override
    public int getMachineId() {
        return machineId;
    }

    @Override
    public void setMachineId(int machineId) {
        this.machineId = machineId;
    }

    @Override
    public String getMachineType() {
        return machineType;
    }

    @Override
    public void setMachineType(String machineType) {
        this.machineType = machineType;
    }

    @Override
    public void addMachine(int machineSerialNumber, String type, String variant, String base_url) {
        String sql = """
                INSERT INTO machines (machineSerialNumber, type, variant, base_url)
                VALUES (?, ?, ?, ?);
                """;
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, machineSerialNumber);
            statement.setString(2, type);
            statement.setString(3, variant);
            statement.setString(4, base_url);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMachine(int machineSerialNumber) {
        String sql = "DELETE FROM machines WHERE machineSerialNumber = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, machineSerialNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connections.remove(machineSerialNumber);
        connectionState.remove(machineSerialNumber);
    }

    @Override
    public CompletableFuture<Void> connectMachine(int machineSerialNumber) {
        return CompletableFuture.runAsync(() -> {
            String sql = "SELECT base_url FROM machines WHERE machineSerialNumber = ?";
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, machineSerialNumber);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) throw new RuntimeException("Ingen maskine fundet med id: " + machineSerialNumber);
                String url = rs.getString("base_url");

                IEmulatorService_Service factory = new IEmulatorService_Service(
                        new URL(url + "?wsdl"),
                        IEmulatorService_Service.SERVICE
                );
                IEmulatorService warehouseService = factory.getBasicHttpBindingIEmulatorService();
                ((BindingProvider) warehouseService).getRequestContext()
                        .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
                connections.put(machineSerialNumber, warehouseService);
                connectionState.put(machineSerialNumber, true);
            } catch (Exception e) {
                throw new RuntimeException("Failed to connect to warehouse " + machineSerialNumber, e);
            }
        });
    }

    @Override
    public void disconnectMachine(int machineSerialNumber) {
        connections.remove(machineSerialNumber);
        connectionState.put(machineSerialNumber, false);
    }

    @Override
    public boolean isConnected(int machineSerialNumber) {
        return connectionState.getOrDefault(machineSerialNumber, false);
    }
}
