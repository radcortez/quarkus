package io.quarkus.vertx.http.runtime;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@Singleton
public class QuarkusWebServerProducer {
    @Produces
    public QuarkusWebServer producer() {
        return QuarkusWebServer.instance();
    }
}
