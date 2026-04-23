package dk.sdu.st4.core.util;

import java.util.*;
import java.util.HashMap;
import java.util.Map;

public class ServiceLocator {

    private static final Map<Class<?>, Object> registry = new HashMap<>();
    private static final Map<String, Object> namedRegistry = new HashMap<>();

    public static <T> void register(Class<T> type, T impl) {
        registry.put(type, impl);
    }

    public static <T> T get(Class<T> type) {
        return type.cast(registry.get(type));
    }

    public static void register(String key, Object impl) {
        namedRegistry.put(key, impl);
    }

    public static <T> T get(String key, Class<T> type) {
        return type.cast(namedRegistry.get(key));
    }
}
