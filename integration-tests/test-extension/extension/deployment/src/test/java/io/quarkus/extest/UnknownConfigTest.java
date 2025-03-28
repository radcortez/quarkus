package io.quarkus.extest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.vertx.http.runtime.VertxHttpBuildTimeConfig;
import io.quarkus.vertx.http.runtime.VertxHttpConfig;

public class UnknownConfigTest {
    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addAsResource("application.properties"))
            .setLogRecordPredicate(record -> record.getLevel().intValue() >= Level.WARNING.intValue())
            .assertLogRecords(logRecords -> {
                Set<String> properties = logRecords.stream().flatMap(
                        logRecord -> Stream.of(Optional.ofNullable(logRecord.getParameters()).orElse(new Object[0])))
                        .map(Object::toString).collect(Collectors.toSet());
                assertTrue(properties.contains("quarkus.unknown.prop"));
                assertFalse(properties.contains("quarkus.build.unknown.prop"));
                assertFalse(properties.contains("proprietary.should.not.report.unknown"));
            });

    @Inject
    Config config;
    @Inject
    VertxHttpBuildTimeConfig httpBuildTimeConfig;
    @Inject
    VertxHttpConfig httpConfig;

    @Test
    void unknown() {
        assertEquals("1234", config.getConfigValue("quarkus.unknown.prop").getValue());
        assertEquals("/1234", httpBuildTimeConfig.nonApplicationRootPath());
        assertEquals(4443, httpConfig.sslPort());
    }
}
