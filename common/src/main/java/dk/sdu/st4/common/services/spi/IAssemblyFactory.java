package dk.sdu.st4.common.services.spi;

import dk.sdu.st4.common.services.IAssembly;

public interface IAssemblyFactory {
    String variant();
    IAssembly create(String serialNo, String brokerUrl) throws Exception;
}
