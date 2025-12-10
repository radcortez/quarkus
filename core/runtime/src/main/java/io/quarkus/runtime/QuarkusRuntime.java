package io.quarkus.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows to register and retrieve instances of an object that represent information only available at runtime. Such
 * object must implement {@link io.quarkus.runtime.QuarkusRuntime.Info}.
 */
public class QuarkusRuntime {
    private static final Map<Class<?>, Info<?>> infos = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> Info<T> getInfo(final Class<T> infoClass) {
        return (Info<T>) infos.get(infoClass);
    }

    // TODO - How to Register these?
    public static <T> void register(final Class<T> infoClass, final Info<T> runtimeInfo) {
        Object mapValue = infos.putIfAbsent(infoClass, runtimeInfo);
        if (mapValue != null) {
            throw new IllegalArgumentException("Info Class already registered " + infoClass);
        }
    }

    public interface Info<T> {
        T get();

        T get(Map<String, Object> properties);
    }
}
