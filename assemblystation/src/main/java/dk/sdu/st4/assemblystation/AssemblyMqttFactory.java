package dk.sdu.st4.assemblystation;

import dk.sdu.st4.common.services.IAssembly;
import dk.sdu.st4.common.services.spi.IAssemblyFactory;

import java.net.URI;

public class AssemblyMqttFactory implements IAssemblyFactory {

    @Override
    public String variant() {
        return "mqtt";
    }

    @Override
    public IAssembly create(String serialNo, String brokerUrl) throws Exception {
        AssemblyConnect connect = (brokerUrl == null || brokerUrl.isBlank())
                ? new AssemblyConnect()
                : buildFromUrl(brokerUrl);
        connect.setMachineId(serialNo);
        connect.connectMachine(serialNo).get();
        return new AssemblyController(connect.getModel());
    }

    private AssemblyConnect buildFromUrl(String brokerUrl) {
        URI uri = URI.create(brokerUrl);
        String host = uri.getHost() != null ? uri.getHost() : "localhost";
        int port = uri.getPort() > 0 ? uri.getPort() : 1883;
        return new AssemblyConnect(host, port);
    }
}
