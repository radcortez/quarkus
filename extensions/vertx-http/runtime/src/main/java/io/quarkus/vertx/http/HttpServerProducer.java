package io.quarkus.vertx.http;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import io.quarkus.runtime.QuarkusRuntime;

@Singleton
public class HttpServerProducer {
    @Produces
    @Singleton
    public HttpServer producer() {
        return QuarkusRuntime.getInfo(HttpServer.class).get();
    }
}
