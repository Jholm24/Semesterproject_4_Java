package dk.sdu.st4.common.db;

import dk.sdu.st4.common.services.IConnect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for machine services that need DB-backed connection management.
 *
 * Handles the machines table (add/remove rows, URL lookup) and tracks
 * connected state. Subclasses implement onConnect / onDisconnect to wire
 * up their protocol-specific client (REST, SOAP, MQTT, …).
 */
public abstract class DBMachineService implements IConnect {

    private final Set<String> connected = ConcurrentHashMap.newKeySet();
    private volatile String serialNo = null;
    private volatile String machineType;

    protected DBMachineService(String machineType) {
        this.machineType = machineType;
    }

    // ── IConnect — state ─────────────────────────────────────────────────────

    @Override public String getMachineId()               { return serialNo; }
    @Override public void   setMachineId(String sn)      { this.serialNo = sn; }
    @Override public String getMachineType()             { return machineType; }
    @Override public void   setMachineType(String type)  { this.machineType = type; }

    // ── IConnect — persistence ───────────────────────────────────────────────

    /** Inserts a new row into the {@code machines} table. */
    @Override
    public void addMachine(String serialNo, String type, String variant, String base_url) {
        String sql = "INSERT INTO machines (serial_no, type, variant, base_url) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serialNo);
            ps.setString(2, type);
            ps.setString(3, variant);
            ps.setString(4, base_url);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add machine " + serialNo, e);
        }
    }

    /** Deletes the row from the {@code machines} table and cleans up in-memory state. */
    @Override
    public void removeMachine(String serialNo) {
        String sql = "DELETE FROM machines WHERE serial_no = ?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serialNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove machine " + serialNo, e);
        }
        connected.remove(serialNo);
        if (serialNo.equals(this.serialNo)) {
            this.serialNo = null;
            onDisconnect(serialNo);
        }
    }

    /**
     * Resolves {@code base_url} from the DB (may be {@code null} for MQTT machines),
     * then delegates to {@link #onConnect(String, String)} for protocol-specific setup.
     */
    @Override
    public void connectMachine(String serialNo) {
        String url = fetchBaseUrl(serialNo);
        try {
            onConnect(serialNo, url);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect machine " + serialNo, e);
        }
        connected.add(serialNo);
        this.serialNo = serialNo;
    }

    @Override
    public void disconnectMachine(String serialNo) {
        connected.remove(serialNo);
        if (serialNo.equals(this.serialNo)) {
            this.serialNo = null;
        }
        onDisconnect(serialNo);
    }

    @Override
    public boolean isConnected(String serialNo) {
        return connected.contains(serialNo);
    }

    // ── hooks for subclasses ─────────────────────────────────────────────────

    /**
     * Called once the DB has resolved the base_url. {@code baseUrl} may be
     * {@code null} for machines that use a shared broker (e.g. MQTT assembly stations).
     * Subclasses should fall back to {@code AppConfig} constants in that case.
     * Throw any Exception to abort the connection attempt.
     */
    protected abstract void onConnect(String serialNo, String baseUrl) throws Exception;

    /**
     * Called when a machine is disconnected or removed. Override to release
     * protocol-specific resources (close socket, unsubscribe MQTT, …).
     */
    protected void onDisconnect(String serialNo) {}

    // ── static DB helpers ────────────────────────────────────────────────────

    /**
     * Returns the serial numbers of all machines with the given type
     * (e.g. {@code "AGV"}, {@code "WAREHOUSE"}, {@code "ASSEMBLY_STATION"}).
     */
    public static List<String> getMachinesByType(String type) {
        String sql = "SELECT serial_no FROM machines WHERE type = ?";
        List<String> result = new ArrayList<>();
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("serial_no"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query machines of type " + type, e);
        }
        return result;
    }

    // ── private DB helper ────────────────────────────────────────────────────

    private String fetchBaseUrl(String serialNo) {
        String sql = "SELECT base_url FROM machines WHERE serial_no = ?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, serialNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("base_url");  // null is allowed — subclass decides
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch base_url for " + serialNo, e);
        }
        throw new IllegalStateException("Machine " + serialNo + " not found in database");
    }
}
