package io.quarkus.vertx.http;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.vertx.http.runtime.QuarkusWebServer;

public class QuarkusWebServerTest {
    @RegisterExtension
    static final QuarkusUnitTest CONFIG = new QuarkusUnitTest();

    @Inject
    QuarkusWebServer webServer;

    @Test
    void ports() {
        Assertions.assertTrue(webServer.getPort() > 0);
    }
}
