package io.quarkus.restclient.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusExtensionTest;

/**
 * The REST Client base URL refers to the HTTP listener by name, so the actual (random) port is resolved when the
 * client is built, without any configuration expression.
 */
class RestClientAddressReferenceTest {
    @RegisterExtension
    static final QuarkusExtensionTest TEST = new QuarkusExtensionTest()
            .withApplicationRoot((jar) -> jar
                    .addClasses(EchoResource.class, EchoClient.class))
            .overrideRuntimeConfigKey("quarkus.http.port", "0")
            .overrideRuntimeConfigKey("quarkus.http.test-port", "0")
            .overrideRuntimeConfigKey("quarkus.rest-client.EchoClient.url", "quarkus://vertx-http");

    @RestClient
    EchoClient echoClient;

    @Test
    void shouldRespond() {
        assertThat(echoClient.echo("Hello")).isEqualTo("Hello");
    }
}
