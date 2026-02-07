package io.quarkus.test.junit;

import static io.quarkus.test.junit.AppMakerHelper.getProfileConfig;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class TestProfileAndProperties {
    private final QuarkusTestProfile testProfile;
    private final Map<String, String> properties;

    TestProfileAndProperties(QuarkusTestProfile testProfile, Map<String, String> properties) {
        this.testProfile = testProfile;
        this.properties = properties != null ? properties : Collections.emptyMap();
    }

    public Optional<QuarkusTestProfile> testProfile() {
        return Optional.ofNullable(testProfile);
    }

    Map<String, String> properties() {
        return Collections.unmodifiableMap(properties);
    }

    Optional<String> configProfile() {
        return testProfile().map(QuarkusTestProfile::getConfigProfile);
    }

    boolean isDisabledGlobalTestResources() {
        return testProfile().map(QuarkusTestProfile::disableGlobalTestResources).orElse(false);
    }

    Optional<String> testProfileClassName() {
        return testProfile().map(testProfile -> testProfile.getClass().getName());
    }

    public static class TestProfileSource {
        private final Path propertiesLocation;
        private final Path propertiesFile;

        TestProfileSource(Path propertiesLocation, Path propertiesFile) {
            this.propertiesLocation = propertiesLocation;
            this.propertiesFile = propertiesFile;
        }

        public Path getPropertiesLocation() {
            return propertiesLocation;
        }

        public Path getPropertiesFile() {
            return propertiesFile;
        }

        public Runnable closeTask() {
            return () -> {
                try {
                    Files.deleteIfExists(propertiesFile);
                } catch (IOException e) {
                    // ignore
                }
                try {
                    Files.deleteIfExists(propertiesLocation);
                } catch (IOException e) {
                    // ignore
                }
            };
        }
    }

    public static TestProfileSource createTestProfileSource(Class<? extends QuarkusTestProfile> profileClass) throws Exception {
        Properties properties = new Properties();
        properties.put("config_ordinal", String.valueOf(Integer.MAX_VALUE - 10000));
        properties.putAll(getProfileConfig(profileClass,
                new ClassCoercingTestProfile(profileClass.getConstructor().newInstance())));

        Path tempDirectory = Files.createTempDirectory("quarkus-test");
        Path propertiesFile = tempDirectory.resolve("application.properties");
        Files.createFile(propertiesFile);

        try (FileOutputStream outputStream = new FileOutputStream(propertiesFile.toFile())) {
            properties.store(outputStream, "");
        }

        return new TestProfileSource(tempDirectory, propertiesFile);
    }
}
