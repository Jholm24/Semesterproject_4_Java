package dk.sdu.st4.core.server;
import dk.sdu.st4.common.services.IConnect;
import dk.sdu.st4.common.services.IWarehouse;
import dk.sdu.st4.common.services.IAssembly;
import dk.sdu.st4.common.services.IAgv;
import java.util.List;
import java.util.ServiceLoader;
import static java.util.stream.Collectors.toList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
public class ModuleConfig {

    @Bean
    public List<IWarehouse> warehouseServiceList(){
        return ServiceLoader.load(IWarehouse.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }

    @Bean
    public List<IAssembly> assemblyServiceList(){
        return ServiceLoader.load(IAssembly.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }
    @Bean
    public List<IAgv> agvServiceList(){
        return ServiceLoader.load(IAgv.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }

    @Bean
    public List<IConnect> connectServiceList(){
        return ServiceLoader.load(IConnect.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }

}
