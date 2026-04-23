package dk.sdu.st4.warehouse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.warehouse.service.IEmulatorService;
import dk.sdu.st4.warehouse.service.IEmulatorService_Service;
import dk.sdu.st4.common.services.IWarehouse;
import dk.sdu.st4.common.services.IConnect;

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
    public void addMachine(int machineId, String url , String machineType) {
        // Add machine to database.
        String sql = """
                INSERT INTO machines (machine_id, machine_type, url)
                VALUES (?, ?, ?);
                """;
        try (Connection connection = DBConnection.getInstance().getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, machineId);
            statement.setString(2, machineType);
            statement.setString(3, url);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeMachine(int machineId) {
        String sql = "DELETE FROM machines WHERE machine_id = ?";
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, machineId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        connections.remove(machineId);
        connectionState.remove(machineId);
    }

    @Override
    public CompletableFuture<Void> connectMachine(int machineId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "SELECT url FROM machines WHERE machine_id = ?";
            try (Connection conn = DBConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, machineId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) throw new RuntimeException("Ingen maskine fundet med id: " + machineId);
                String url = rs.getString("url");

                IEmulatorService_Service factory = new IEmulatorService_Service(
                        new URL(url + "?wsdl"),
                        IEmulatorService_Service.SERVICE
                );
                IEmulatorService warehouseService = factory.getBasicHttpBindingIEmulatorService();
                ((BindingProvider) warehouseService).getRequestContext()
                        .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
                connections.put(machineId, warehouseService);
                connectionState.put(machineId, true);
            } catch (Exception e) {
                throw new RuntimeException("Failed to connect to warehouse " + machineId, e);
            }
        });
    }

    @Override
    public void disconnectMachine(int machineId) {
        connections.remove(machineId);
        connectionState.put(machineId, false);
    }

    @Override
    public boolean isConnected(int machineId) {
        return connectionState.getOrDefault(machineId, false);
    }

    @Override
    public int getMachineId() {
        //TODO:
        return 0;

        //


    }

    @Override
    public void setMachineId(int machineId) {
        //TODO:
    }

    @Override
    public String getMachineType() {
        //Tjek igennem database for at finde maskinens URL HvaD SKAL DEN BRUGES TIL ALTSÅÅÅÅÅ
        return "Parts";
    }
    @Override
    public void setMachineType(String machineType) {
        // TODO
    }

    // IWarehouse
    @Override
    public void PickItem(int trayID, int machineID) {
        connections.get(machineID).pickItem(trayID);
    }

    @Override
    public void InsertItem(int trayID, String name, int machineID) {
        connections.get(machineID).insertItem(trayID, name);
    }

    @Override
    public void GetInventory(int machineID) {
        connections.get(machineID).getInventory();
    }

    @Override
    public int GetState(int machineID) {
        try {
            String json = connections.get(machineID).getInventory();
            JsonNode root = mapper.readTree(json);
            return root.get("State").asInt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read State from warehouse " + machineID, e);
        }
    }
}