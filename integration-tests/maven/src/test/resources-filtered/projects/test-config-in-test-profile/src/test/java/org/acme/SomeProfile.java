package org.acme;

import java.util.Map;

import io.smallrye.config.Config;
import io.quarkus.test.junit.QuarkusTestProfile;

public class SomeProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        // Access config
        Config.get().getPropertyNames();
        return QuarkusTestProfile.super.getConfigOverrides();
    }
}
