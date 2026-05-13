package dk.sdu.st4.agv.service;

import dk.sdu.st4.agv.client.AgvClient;
import dk.sdu.st4.common.config.AppConfig;
import dk.sdu.st4.common.services.IAgv;
import dk.sdu.st4.common.services.spi.IAgvFactory;

public class AgvRestFactory implements IAgvFactory {

    @Override
    public String variant() {
        return "rest";
    }

    @Override
    public IAgv create(String serialNo, String baseUrl) {
        return new AgvClient(baseUrl != null ? baseUrl : AppConfig.AGV_BASE_URL);
    }
}
