package dk.sdu.st4.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IConnect;
import dk.sdu.st4.common.services.IWarehouse;
import dk.sdu.st4.warehouse.service.IEmulatorService;
import dk.sdu.st4.warehouse.service.IEmulatorService_Service;
import jakarta.xml.ws.BindingProvider;
import org.glassfish.jaxb.core.v2.TODO;

import java.sql.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WarehouseClient implements IWarehouse, IConnect {

    private static WarehouseClient instance;
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, IEmulatorService> connections = new HashMap<>();
    private final Map<Integer, Boolean> connectionState = new HashMap<>();

    //singleton pattern
    private WarehouseClient() {
    }

    //How to acces object.
    public static WarehouseClient getInstance() {
        if (instance == null) {
            instance = new WarehouseClient();
        }
        return instance;
    }

    // IConnect

    @Override // method for adding Warehouse machines to the hashmap.
    public void addMachine(int machineSerialNumber,String type,String variant, String base_url) {
        // Add machine to database.
        String sql = """
                INSERT INTO machines (machineSerialNumber, type,variant, base_url)
                VALUES (?, ?, ?,?);
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

    @Override
    public int getMachineId() {
        //TODO:
        return 0;

        //


    }

    @Override
    public void setMachineId(int machineSerialNumber) {
        //TODO:
    }

    @Override
    public String getMachineType() {
        //Tjek igennem database for at finde maskinens URL HvaD SKAL DEN BRUGES TIL ALTSÅÅÅÅÅ
        return "Parts";
    }
    @Override
    public void setMachineType(String type) {
        // TODO
    }

    // IWarehouse
    @Override
    public void PickItem(int trayID, int machieSerialNumber) {
        connections.get(machieSerialNumber).pickItem(trayID);
    }

    @Override
    public void InsertItem(int trayID, String name, int machineSerialNumber) {
        connections.get(machineSerialNumber).insertItem(trayID, name);
    }

    @Override
    public void GetInventory(int machineSerialNumber) {
        connections.get(machineSerialNumber).getInventory();
    }

    @Override
    public int GetState(int machineSerialNumber) {
        try {
            String json = connections.get(machineSerialNumber).getInventory();
            JsonNode root = mapper.readTree(json);
            return root.get("State").asInt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read State from warehouse " + machineSerialNumber, e);
        }
    }
}