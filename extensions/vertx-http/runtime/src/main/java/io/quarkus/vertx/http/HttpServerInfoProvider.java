package io.quarkus.vertx.http;

import io.quarkus.bootstrap.runtime.InfoProvider;
import io.quarkus.bootstrap.runtime.QuarkusRuntime;

public class HttpServerInfoProvider implements InfoProvider {
    @Override
    public void register(QuarkusRuntime quarkusRuntime) {
        quarkusRuntime.registerInfo(HttpServer.HTTP_SERVER, HttpServer.INFO);
    }
}
