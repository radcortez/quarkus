package io.quarkus.runtime;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuarkusRuntime {
    private static final QuarkusRuntime INSTANCE = new QuarkusRuntime();

    private final Map<RuntimeKey<?>, Object> values = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T register(RuntimeKey<T> key, T value) {
        Object mapValue = values.computeIfAbsent(key, k -> value);
        if (!mapValue.equals(value)) {
            throw new IllegalStateException("Key already registered " + key + " with value " + mapValue);
        }
        return (T) mapValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(RuntimeKey<T> key) {
        return (T) values.get(key);
    }

    public static QuarkusRuntime instance() {
        return INSTANCE;
    }

    public interface RuntimeKey<T> {
        String key();

        static RuntimeKey<Integer> intKey(final String key) {
            return new RuntimeKeyImpl<>(key);
        }

        record RuntimeKeyImpl<T>(String key) implements RuntimeKey<T> {
        }
    }
}
