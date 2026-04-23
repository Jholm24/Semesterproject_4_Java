package dk.sdu.st4.core.util;

import dk.sdu.st4.common.Interfaces.IAssembly;
import dk.sdu.st4.common.Interfaces.IConnect;
import dk.sdu.st4.assemblystation.client.AssemblyController;
import dk.sdu.st4.core.registries.AssemblyRegistry;

public class AppConfig {
    public static void configure() throws Exception {
        AssemblyRegistry.getInstance().configure();
    }
}