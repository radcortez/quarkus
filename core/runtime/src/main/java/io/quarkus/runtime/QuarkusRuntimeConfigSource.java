package io.quarkus.runtime;

import java.util.Set;

import io.quarkus.bootstrap.runtime.QuarkusRuntime;
import io.quarkus.bootstrap.runtime.QuarkusRuntime.Info;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilderCustomizer;
import io.smallrye.config.common.AbstractConfigSource;

/**
 * A {@link org.eclipse.microprofile.config.spi.ConfigSource} to bridge between the Config System and
 * {@link io.quarkus.bootstrap.runtime.QuarkusRuntime}.
 * <p>
 * While {@link QuarkusRuntimeImpl} shouldn't be exposed in the Config System, this is intended to
 * work as a temporary compatibility layer, since until the introduction of
 * {@link io.quarkus.bootstrap.runtime.QuarkusRuntime}, the norm was to use {@link io.smallrye.config.SmallRyeConfig}
 * and System Properties to relay this kind of information.
 * <p>
 * This should be kept until we decide on an alternate solution in the discussion
 * <a href="https://github.com/quarkusio/quarkus/discussions/46915">#46915</a>.
 */
@SuppressWarnings("unused")
public class QuarkusRuntimeConfigSource extends AbstractConfigSource {
    private final QuarkusRuntime quarkusRuntime;

    QuarkusRuntimeConfigSource(final QuarkusRuntime quarkusRuntime) {
        // ordinal just a bit lower than Build Time Runtime fixed source
        super("Quarkus Runtime Values", Integer.MAX_VALUE - 10);
        this.quarkusRuntime = quarkusRuntime;
    }

    @Override
    public Set<String> getPropertyNames() {
        return Set.of();
    }

    @Override
    public String getValue(String propertyName) {
        Info<?> value = quarkusRuntime.get(propertyName);
        // TODO - We may require to convert this to the expected config string
        return value != null ? value.get(quarkusRuntime).toString() : null;
    }

    public static SmallRyeConfigBuilderCustomizer customizer(QuarkusRuntime quarkusRuntime) {
        return new SmallRyeConfigBuilderCustomizer() {
            @Override
            public void configBuilder(SmallRyeConfigBuilder builder) {
                builder.withSources(new QuarkusRuntimeConfigSource(quarkusRuntime));
            }
        };
    }
}
