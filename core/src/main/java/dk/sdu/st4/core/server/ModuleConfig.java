package dk.sdu.st4.core.server;

import dk.sdu.st4.common.services.IAgvRegistry;
import dk.sdu.st4.common.services.IAssemblyRegistry;
import dk.sdu.st4.common.services.IWarehouseRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.ServiceLoader;

@Configuration
public class ModuleConfig {

    @Bean
    public Optional<IAgvRegistry> agvRegistry() {
        Optional<IAgvRegistry> reg = ServiceLoader.load(IAgvRegistry.class).findFirst();
        if (reg.isPresent()) {
            IAgvRegistry r = reg.get();
            r.loadFromDb();
            for (int i = 0; i < 20; i++) r.connectNext();
        } else {
            System.out.println("[ModuleConfig] IAgvRegistry ikke fundet — AGV-modul mangler i mods-mvn");
        }
        return reg;
    }

    @Bean
    public Optional<IWarehouseRegistry> warehouseRegistry() {
        Optional<IWarehouseRegistry> reg = ServiceLoader.load(IWarehouseRegistry.class).findFirst();
        if (reg.isPresent()) {
            reg.get().loadFromDb();
        } else {
            System.out.println("[ModuleConfig] IWarehouseRegistry ikke fundet — Warehouse-modul mangler i mods-mvn");
        }
        return reg;
    }

    @Bean
    public Optional<IAssemblyRegistry> assemblyRegistry() throws Exception {
        Optional<IAssemblyRegistry> reg = ServiceLoader.load(IAssemblyRegistry.class).findFirst();
        if (reg.isPresent()) {
            IAssemblyRegistry r = reg.get();
            r.loadFromDb();
            for (int i = 0; i < 20; i++) r.connectNext();
        } else {
            System.out.println("[ModuleConfig] IAssemblyRegistry ikke fundet — Assembly-modul mangler i mods-mvn");
        }
        return reg;
    }
}
