import dk.sdu.st4.common.services.IAssembly;
import dk.sdu.st4.common.services.IConnect;

module dk.sdu.st4.assemblystation {
    exports dk.sdu.st4.assemblystation;
    requires dk.sdu.st4.common;
    requires org.eclipse.paho.client.mqttv3;
    requires com.google.gson;
    requires java.sql;
    provides IAssembly with dk.sdu.st4.assemblystation.AssemblyController;
    provides IConnect with dk.sdu.st4.assemblystation.
}