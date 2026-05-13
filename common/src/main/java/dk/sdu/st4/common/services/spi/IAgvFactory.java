package dk.sdu.st4.common.services.spi;

import dk.sdu.st4.common.services.IAgv;

public interface IAgvFactory {
    String variant();
    IAgv create(String serialNo, String baseUrl) throws Exception;
}
