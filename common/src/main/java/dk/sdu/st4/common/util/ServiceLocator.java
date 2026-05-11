package dk.sdu.st4.common.util;
import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
public enum ServiceLocator {
    INSTANCE;


    private static final Map< Class, ServiceLoader> serviceLoadermap = new HashMap<>();
    private final ModuleLayer layer;

    ServiceLocator(){
        try {
            Path plugginDir = Paths.get("plugins"); //Where JARs are located.
            ModuleFinder plugginFinder = ModuleFinder.of(plugginDir); // Search for plugins in directory

            List<String> plugins = plugginFinder
                    .findAll()
                    .stream()
                    .map(ModuleReference::descriptor)
                    .map(ModuleDescriptor::name)
                    .collect(Collectors.toList());

            Configuration plugginConfiguration = ModuleLayer
                    .boot()
                    .configuration()
                    .resolve(plugginFinder,ModuleFinder.of(),plugins);

            layer = ModuleLayer // creates moduler layer for plugins.
                    .boot()
                    .defineModulesWithManyLoaders(plugginConfiguration , ClassLoader.getSystemClassLoader());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public <T> List<T> locateAll(Class<T> serviceClass) {
        ServiceLoader<T> loader = serviceLoadermap.get(serviceClass);
        if (loader == null) {
            loader = ServiceLoader.load(layer ,serviceClass);
            serviceLoadermap.put(serviceClass, loader);
        }

        List<T> list = new ArrayList<T>();
        if (loader!=null){
            try{
                for(T instance : loader){
                    list.add(instance);}
            }
            catch (ServiceConfigurationError serviceError){
                serviceError.printStackTrace();
            }
        }
        return list;
    }
 }

