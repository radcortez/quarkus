package io.quarkus.rest.client.reactive;

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
            .withApplicationRoot((jar) -> jar.addClasses(HelloClient2.class, HelloResource.class))
            .overrideRuntimeConfigKey("quarkus.http.port", "0")
            .overrideRuntimeConfigKey("quarkus.http.test-port", "0")
            // the client has no class-level @Path, so the path of the reference is the resource path
            .overrideRuntimeConfigKey("quarkus.rest-client.hello2.url", "quarkus://vertx-http/hello");

    @RestClient
    HelloClient2 helloClient;

    @Test
    void shouldRespond() {
        assertThat(helloClient.echo("w0rld")).isEqualTo("hello, w0rld");
    }
}
