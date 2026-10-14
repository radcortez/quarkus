package org.acme;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;
import io.smallrye.config.Config;

public class SomeProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {

        // Access config
        Config.get().getPropertyNames();

        return QuarkusTestProfile.super.getConfigOverrides();
    }
}
