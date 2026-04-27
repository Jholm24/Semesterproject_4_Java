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
    private final Map<String, IEmulatorService> connections = new HashMap<>();
    private final Map<String, Boolean> connectionState = new HashMap<>();

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
    public void addMachine(String serialNumber,String type,String variant, String base_url) {
        // Add machine to database.
        String sql = """
                INSERT INTO machines (serial_no, type, variant, base_url)
                VALUES (?, ?, ?,?);
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

    @Override
    public String getMachineId() {
        //TODO:
        return null;

        //


    }

    @Override
    public void setMachineId(String serialNumber) {
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
    public void PickItem(int trayID, String serialNumber) {
        connections.get(serialNumber).pickItem(trayID);
    }

    @Override
    public void InsertItem(int trayID, String name, String serialNumber) {
        connections.get(serialNumber).insertItem(trayID, name);
    }

    @Override
    public void GetInventory(String serialNumber) {
        connections.get(serialNumber).getInventory();
    }

    @Override
    public int GetState(String serialNumber) {
        try {
            String json = connections.get(serialNumber).getInventory();
            JsonNode root = mapper.readTree(json);
            return root.get("State").asInt();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read State from warehouse " + serialNumber, e);
        }
    }
}