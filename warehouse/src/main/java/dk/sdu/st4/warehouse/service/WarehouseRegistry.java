package dk.sdu.st4.warehouse.service;

import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IWarehouse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WarehouseRegistry {

    private final Map<String, IWarehouse> services = new ConcurrentHashMap<>();

    public void loadFromDb() {
        String sql = "SELECT serial_no FROM machines WHERE type = 'WAREHOUSE'";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String sn = rs.getString("serial_no");
                try {
                    connect(sn);
                } catch (Exception e) {
                    System.err.println("[WarehouseRegistry] Failed to connect " + sn + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load warehouses from DB", e);
        }
    }

    public void connect(String serialNumber) {
        String sql = "SELECT base_url FROM machines WHERE serial_no = ?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serialNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Warehouse " + serialNumber + " not found in DB");
                String baseUrl = rs.getString("base_url");
                WarehouseConnect connect = new WarehouseConnect(baseUrl);
                connect.connectMachine(serialNumber);
                services.put(serialNumber, new WarehouseService(connect.getModel()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect warehouse " + serialNumber, e);
        }
    }

    public void disconnect(String serialNumber) {
        services.remove(serialNumber);
    }

    public IWarehouse getWarehouse(String serialNumber) {
        return services.get(serialNumber);
    }

}
