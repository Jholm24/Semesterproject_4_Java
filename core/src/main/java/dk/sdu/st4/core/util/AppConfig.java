package dk.sdu.st4.core.util;

import dk.sdu.st4.app.Registries.AssemblyRegistry;
import dk.sdu.st4.common.services.IAssembly;
import dk.sdu.st4.common.services.IConnect;
import dk.sdu.st4.assemblystation.AssemblyController;

public class AppConfig {
    public static void configure() throws Exception {
        AssemblyRegistry.getInstance().configure();
    }
}