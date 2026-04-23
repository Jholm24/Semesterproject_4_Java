package dk.sdu.st4.agv.service;

import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.data.AgvStatus;
import org.junit.jupiter.api.Test;

/**
 * Hits the real AGV Docker container at localhost:8082.
 * Requires `docker compose up -d` before running.
 */
class AgvIntegrationTest {

    @Test
    void getStatus_printsRealAgvResponse() throws Exception {
        AgvServiceImpl service = new AgvServiceImpl(AppConfig.AGV_BASE_URL);

        AgvStatus status = service.getStatus();

        System.out.println("=== AGV Real Connection Response ===");
        System.out.println("Battery   : " + status.getBattery() + "%");
        System.out.println("Program   : " + status.getProgramName());
        System.out.println("State     : " + status.getState());
        System.out.println("Timestamp : " + status.getTimestamp());
        System.out.println("====================================");
    }
}
