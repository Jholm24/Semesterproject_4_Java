package dk.sdu.st4.core.server;

import dk.sdu.st4.common.services.IAgvRegistry;
import dk.sdu.st4.common.services.IAssemblyRegistry;
import dk.sdu.st4.common.services.IWarehouseRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ServiceLoader;

@Configuration
public class ModuleConfig {

    @Bean
    public IAgvRegistry agvRegistry() {
        IAgvRegistry registry = ServiceLoader.load(IAgvRegistry.class).findFirst().orElseThrow();
        registry.loadFromDb();
        return registry;
    }

    @Bean
    public IWarehouseRegistry warehouseRegistry() {
        IWarehouseRegistry registry = ServiceLoader.load(IWarehouseRegistry.class).findFirst().orElseThrow();
        registry.loadFromDb();
        return registry;
    }

    @Bean
    public IAssemblyRegistry assemblyRegistry() throws Exception {
        IAssemblyRegistry registry = ServiceLoader.load(IAssemblyRegistry.class).findFirst().orElseThrow();
        registry.loadFromDb();
        return registry;
    }
}
