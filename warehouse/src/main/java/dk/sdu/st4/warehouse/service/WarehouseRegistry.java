package dk.sdu.st4.warehouse.service;

import dk.sdu.st4.common.db.DBConnection;
import dk.sdu.st4.common.services.IWarehouse;
import dk.sdu.st4.common.services.spi.IWarehouseFactory;
import dk.sdu.st4.common.services.IWarehouseRegistry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WarehouseRegistry implements IWarehouseRegistry {

    private final Map<String, IWarehouse> bySerialNo      = new ConcurrentHashMap<>();
    private final Map<String, IWarehouse> byVariant       = new ConcurrentHashMap<>();
    private final Map<String, String>     variantBySerial = new ConcurrentHashMap<>();

    private final Map<String, IWarehouseFactory> factories =
            ServiceLoader.load(IWarehouseFactory.class).stream()
                    .map(ServiceLoader.Provider::get)
                    .collect(Collectors.toMap(IWarehouseFactory::variant, f -> f));

    @Override
    public void loadFromDb() {
        String sql = "SELECT DISTINCT m.serial_no FROM machines m " +
                     "INNER JOIN line_machines lm ON m.serial_no = lm.serial_no " +
                     "WHERE m.type = 'WAREHOUSE'";
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

    @Override
    public void connect(String serialNumber) {
        String sql = "SELECT base_url, variant, protocol FROM machines WHERE serial_no = ?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serialNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalStateException("Warehouse " + serialNumber + " not found in DB");
                String baseUrl  = rs.getString("base_url");
                String variant  = rs.getString("variant");
                String protocol = rs.getString("protocol");
                IWarehouseFactory factory = factories.get(protocol);
                if (factory == null) {
                    throw new IllegalStateException("No IWarehouseFactory for protocol '" + protocol
                            + "' (serial " + serialNumber + ") — vendor module missing in mods-mvn?");
                }
                IWarehouse service = factory.create(serialNumber, baseUrl);
                bySerialNo.put(serialNumber, service);
                if (variant != null && !variant.isBlank()) {
                    byVariant.put(variant, service);
                    variantBySerial.put(serialNumber, variant);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect warehouse " + serialNumber, e);
        }
    }

    @Override
    public List<Map<String, Object>> getMachinesStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String sn : bySerialNo.keySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("serialNo",       sn);
            m.put("poolStatus",     "idle");
            m.put("variant",        variantBySerial.getOrDefault(sn, ""));
            m.put("warehouseState", 0);
            result.add(m);
        }
        return result;
    }

    @Override
    public void disconnect(String serialNumber) {
        IWarehouse removed = bySerialNo.remove(serialNumber);
        if (removed != null) byVariant.values().remove(removed);
        variantBySerial.remove(serialNumber);
    }

    @Override
    public IWarehouse getWarehouse(String serialNumber) {
        return bySerialNo.get(serialNumber);
    }

    @Override
    public IWarehouse getWarehouseByVariant(String variant) {
        return byVariant.get(variant);
    }
}
