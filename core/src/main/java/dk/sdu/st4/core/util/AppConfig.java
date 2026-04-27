package dk.sdu.st4.core.util;

import dk.sdu.st4.app.Registries.AssemblyRegistry;

public class AppConfig {
    public static void configure() throws Exception {
        AssemblyRegistry.getInstance().configure();
    }
}