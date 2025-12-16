package io.quarkus.bootstrap.app;

import java.lang.annotation.Annotation;
import java.util.Optional;

import io.quarkus.bootstrap.runtime.QuarkusRuntime;

public interface RunningQuarkusApplication extends AutoCloseable {
    ClassLoader getClassLoader();

    @Override
    void close() throws Exception;

    @Deprecated(forRemoval = true)
    <T> Optional<T> getConfigValue(String key, Class<T> type);

    @Deprecated(forRemoval = true)
    Iterable<String> getConfigKeys();

    /**
     * Looks up an instance from the CDI container of the running application.
     *
     * @param clazz The class
     * @param qualifiers The qualifiers
     * @return The instance or null
     */
    Object instance(Class<?> clazz, Annotation... qualifiers);

    QuarkusRuntime quarkusRuntime();
}
