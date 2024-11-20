package io.quarkus.runtime.configuration;

import static io.quarkus.runtime.ConfigConfig.BuildTimeMismatchAtRuntime;
import static io.quarkus.runtime.ConfigConfig.BuildTimeMismatchAtRuntime.fail;
import static io.quarkus.runtime.ConfigConfig.BuildTimeMismatchAtRuntime.warn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.logging.Logger;

import io.quarkus.runtime.ConfigConfig;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilderCustomizer;

@Recorder
public class ConfigRecorder {
    private static final Logger log = Logger.getLogger(ConfigRecorder.class);

    public void handleConfigChange(Map<String, ConfigValue> buildTimeRuntimeValues) {
        SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        // Disable the BuildTime RunTime Fixed (has the highest ordinal), because a lookup will get the expected value,
        // and we have no idea if the user tried to override it in another source.
        Optional<ConfigSource> builtTimeRunTimeFixedConfigSource = config.getConfigSource("BuildTime RunTime Fixed");
        if (builtTimeRunTimeFixedConfigSource.isPresent()) {
            ConfigSource configSource = builtTimeRunTimeFixedConfigSource.get();
            if (configSource instanceof DisableableConfigSource) {
                ((DisableableConfigSource) configSource).disable();
            }
        }

        List<String> mismatches = new ArrayList<>();
        for (Map.Entry<String, ConfigValue> entry : buildTimeRuntimeValues.entrySet()) {
            ConfigValue currentValue = config.getConfigValue(entry.getKey());
            // Check for changes. Also, we only have a change if the source ordinal is higher
            // The config value can be null (for ex. if the property uses environment variables not available at build time)
            if (currentValue.getValue() != null && !Objects.equals(entry.getValue().getValue(), currentValue.getValue())
                    && entry.getValue().getSourceOrdinal() < currentValue.getSourceOrdinal()) {
                mismatches.add(
                        " - " + entry.getKey() + " is set to '" + currentValue.getValue()
                                + "' but it is build time fixed to '"
                                + entry.getValue().getValue() + "'. Did you change the property " + entry.getKey()
                                + " after building the application?");
            }
        }

        // Enable the BuildTime RunTime Fixed. It should be fine doing these operations, because this is on startup
        if (builtTimeRunTimeFixedConfigSource.isPresent()) {
            ConfigSource configSource = builtTimeRunTimeFixedConfigSource.get();
            if (configSource instanceof DisableableConfigSource) {
                ((DisableableConfigSource) configSource).enable();
            }
        }

        if (!mismatches.isEmpty()) {
            String msg = "Build time property cannot be changed at runtime:\n" + String.join("\n", mismatches);
            // TODO - This should use ConfigConfig, but for some reason, the test fails sometimes with mapping not found when looking ConfigConfig
            BuildTimeMismatchAtRuntime buildTimeMismatchAtRuntime = config
                    .getOptionalValue("quarkus.config.build-time-mismatch-at-runtime", BuildTimeMismatchAtRuntime.class)
                    .orElse(warn);

            // Maybe it is a different instance?
            SmallRyeConfig quarkusConfig = new QuarkusConfigFactory().getConfigFor(null, null);
            if (!config.equals(quarkusConfig)) {
                throw new IllegalStateException("SmallRyeConfig Classloaders mismatch!");
            }

            // Is it missing from the customizer?
            SmallRyeConfigBuilder mappingBuilder = new SmallRyeConfigBuilder();
            AbstractConfigBuilder.withCustomizer(mappingBuilder, "io.quarkus.runtime.generated.RunTimeConfig");
            mappingBuilder.withCustomizers(new MissingMappingConfigBuilderCustomizer());
            mappingBuilder.build();

            // Does this fail even if present in the builder?
            config.getConfigMapping(ConfigConfig.class);

            if (fail.equals(buildTimeMismatchAtRuntime)) {
                throw new IllegalStateException(msg);
            } else if (warn.equals(buildTimeMismatchAtRuntime)) {
                log.warn(msg);
            }
        }
    }

    static class MissingMappingConfigBuilderCustomizer implements SmallRyeConfigBuilderCustomizer {
        @Override
        public void configBuilder(final SmallRyeConfigBuilder builder) {
            Map<Class<?>, Set<String>> mappings = builder.getMappingsBuilder().getMappings();
            for (Map.Entry<Class<?>, Set<String>> entry : mappings.entrySet()) {
                for (String prefix : entry.getValue()) {
                    log.info("Found mapping " + entry.getKey() + " with prefix " + prefix);
                }
            }
        }

        @Override
        public int priority() {
            return Integer.MAX_VALUE;
        }
    }

    public void handleNativeProfileChange(List<String> buildProfiles) {
        SmallRyeConfig config = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class);
        List<String> runtimeProfiles = config.getProfiles();

        if (buildProfiles.size() != runtimeProfiles.size()) {
            log.warn(
                    "The profile '" + buildProfiles + "' used to build the native image is different from the runtime profile '"
                            + runtimeProfiles + "'. This may lead to unexpected results.");
            return;
        }

        for (int i = 0; i < buildProfiles.size(); i++) {
            String buildProfile = buildProfiles.get(i);
            String runtimeProfile = runtimeProfiles.get(i);

            if (!buildProfile.equals(runtimeProfile)) {
                log.warn("The profile '" + buildProfile
                        + "' used to build the native image is different from the runtime profile '" + runtimeProfile
                        + "'. This may lead to unexpected results.");
            }
        }
    }

    public void unknownConfigFiles() throws Exception {
        ConfigDiagnostic.unknownConfigFiles(ConfigDiagnostic.configFilesFromLocations());
    }

    public void releaseConfig(ShutdownContext shutdownContext) {
        // This is mostly useful to handle restarts in Dev/Test mode.
        // While this may seem to duplicate code in IsolatedDevModeMain,
        // it actually does not because it operates on a different instance
        // of QuarkusConfigFactory from a different classloader.
        shutdownContext.addLastShutdownTask(QuarkusConfigFactory::releaseTCCLConfig);
    }
}
