import dk.sdu.st4.common.services.IAssembly;
import dk.sdu.st4.common.services.IAssemblyRegistry;
import dk.sdu.st4.common.services.IConnect;
import dk.sdu.st4.common.services.spi.IAssemblyFactory;
import dk.sdu.st4.assemblystation.AssemblyConnect;
import dk.sdu.st4.assemblystation.AssemblyController;
import dk.sdu.st4.assemblystation.AssemblyMqttFactory;
import dk.sdu.st4.assemblystation.AssemblyRegistry;

module dk.sdu.st4.assemblystation {
    exports dk.sdu.st4.assemblystation;
    requires dk.sdu.st4.common;
    requires org.eclipse.paho.client.mqttv3;
    requires com.google.gson;
    requires java.sql;

    uses IAssemblyFactory;

    provides IAssembly         with AssemblyController;
    provides IConnect          with AssemblyConnect;
    provides IAssemblyRegistry with AssemblyRegistry;
    provides IAssemblyFactory  with AssemblyMqttFactory;
}
