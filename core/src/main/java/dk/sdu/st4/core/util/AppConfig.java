package dk.sdu.st4.core.util;

import dk.sdu.st4.common.Interfaces.IAssembly;
import dk.sdu.st4.common.Interfaces.IConnect;
import dk.sdu.st4.assemblystation.client.AssemblyController;

public class AppConfig {
    public static void configure() throws Exception {
        ServiceLocator.register("assembly-1", new AssemblyController(1883));
        //ServiceLocator.register("assembly-2", new AssemblyController(1884));
        //ServiceLocator.register("assembly-3", new AssemblyController(1885));
    }
}