import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.warehouse.service.IEmulatorService;
import dk.sdu.st4.warehouse.service.IEmulatorService_Service;
import dk.sdu.st4.common.Interfaces.IWarehouse;
import dk.sdu.st4.common.Interfaces.IConnect;

import jakarta.xml.ws.BindingProvider;
import org.glassfish.jaxb.core.v2.TODO;

import java.sql.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WarehouseClient implements IWarehouse, IConnect {

    //singleton pattern
    private static WarehouseClient instance;
    private static final ObjectMapper mapper = new ObjectMapper();
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
    public void addMachine(int machineId, String url) {
        String machineType = getMachineType();
        // Timestamp createdAt = new Timestamp(new Date());

        String sql = """
                INSERT INTO warehouse_machines (machine_id, machine_type, url)
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

    @Override // method for removing machines from the hashmap.
    public void removeMachine(int machineId) {
//        connections.remove(machineId);
//        endpoints.remove(machineId);
//        connectionState.remove(machineId);
    }

    @Override
    public CompletableFuture<Void> connectMachine(int machineId) {
        return CompletableFuture.runAsync(() -> {
            try {
                String url = endpoints.get(machineId);
                IEmulatorService_Service factory = new IEmulatorService_Service(
                        new URL(url + "?wsdl"),
                        IEmulatorService_Service.SERVICE
                );
                IEmulatorService proxy = factory.getBasicHttpBindingIEmulatorService();
                ((BindingProvider) proxy).getRequestContext() // bindingProvider used to override the hardcoded port, from generated code(Jakarta)
                        .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
                connections.put(machineId, proxy);
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
        //Tjek igennem database for at finde maskinens URL
        return "Parts";
    }

    public String getWarehouseType() {
        //warehose har 3 tryper
        //Parts
        //FinishedProducts
        //FailedProducts

        //Vi skal give maskinen en type når den laves
        //

        return "Parts";
    }


    @Override
    public void setMachineType(String machineType) {
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
