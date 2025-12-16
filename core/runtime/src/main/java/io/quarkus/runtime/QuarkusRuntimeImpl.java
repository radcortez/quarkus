package io.quarkus.runtime;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkus.bootstrap.runtime.InfoProvider;
import io.quarkus.bootstrap.runtime.QuarkusRuntime;
import io.quarkus.bootstrap.runtime.QuarkusRuntime.Info.SimpleInfo;

public class QuarkusRuntimeImpl implements QuarkusRuntime {
    private final Map<String, Info<?>> values = new ConcurrentHashMap<>();

    public <T> void register(final RuntimeKey<T> key, final T value) {
        registerInfo(key, SimpleInfo.of(value));
    }

    public <T> void registerInfo(final RuntimeKey<T> key, final Info<T> info) {
        Info<?> mapValue = values.putIfAbsent(key.key(), info);
        if (mapValue != null) {
            throw new IllegalArgumentException("Key already registered " + key.key());
        }
    }

    public <T> boolean containsKey(final RuntimeKey<T> key) {
        return values.containsKey(key.key());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final RuntimeKey<T> key) {
        Info<T> info = (Info<T>) values.get(key.key());
        if (info == null) {
            throw new IllegalArgumentException("Key " + key.key() + " not found");
        }
        return info.get(this);
    }

    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(final RuntimeKey<T> key, final T defaultValue) {
        Info<T> info = (Info<T>) values.get(key.key());
        return info == null ? defaultValue : info.get(this);
    }

    @Override
    public Info<?> get(String key) {
        return values.get(key);
    }

    public static class Builder {
        private boolean discoverInfos;

        public Builder addDiscoveredInfos() {
            this.discoverInfos = true;
            return this;
        }

        public QuarkusRuntime build() {
            QuarkusRuntimeImpl quarkusRuntime = new QuarkusRuntimeImpl();
            if (discoverInfos) {
                ServiceLoader<InfoProvider> infoProviders = ServiceLoader.load(InfoProvider.class);
                for (InfoProvider infoProvider : infoProviders) {
                    infoProvider.register(quarkusRuntime);
                }
            }
            return quarkusRuntime;
        }
    }
}
