package dk.sdu.st4.app.Registries;

import dk.sdu.st4.common.services.IAgv;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pulls AGV machines from the live database and verifies they load correctly.
 * Requires `docker compose up -d` (PostgreSQL on port 5432) before running.
 */
class AgvRegistryDbTest {

    @Test
    void loadFromDb_connectsAllAgvMachines() {
        AgvRegistry registry = new AgvRegistry();

        registry.loadFromDb();

        Set<String> available = registry.getAvailable();

        System.out.println("=== AGV machines loaded from DB ===");
        available.forEach(sn -> System.out.println("  " + sn));
        System.out.println("Total: " + available.size());
        System.out.println("===================================");

        assertFalse(available.isEmpty(), "Expected at least one AGV in the machines table");
        available.forEach(sn -> assertTrue(registry.isConnected(sn), sn + " should be marked connected"));
    }

    @Test
    void acquire_returnsConnectedAgv() {
        AgvRegistry registry = new AgvRegistry();
        registry.loadFromDb();

        IAgv agv = registry.acquire();

        assertNotNull(agv, "acquire() should return an AGV when machines are loaded");

        registry.release(agv);
    }
}
