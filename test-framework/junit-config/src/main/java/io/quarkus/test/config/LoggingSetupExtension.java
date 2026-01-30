package io.quarkus.test.config;

import org.junit.jupiter.api.extension.Extension;

import io.quarkus.runtime.logging.LoggingSetupRecorder;
import io.smallrye.config.Config;

/**
 * A global JUnit extension that enables/sets up basic logging if logging has not already been set up.
 * <p/>
 * This is useful for getting log output from non-Quarkus tests (if executed separately or before the first Quarkus
 * test), but also for getting instant log output from {@code QuarkusTestResourceLifecycleManagers} etc.
 */
public class LoggingSetupExtension implements Extension {
    public LoggingSetupExtension() {
        // TODO - radcortez - Register config directly instead of relying in automatic creation
        Config.getOrCreate();
        LoggingSetupRecorder.handleFailedStart();
    }
}
