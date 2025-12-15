package io.quarkus.vertx.http;

import static io.quarkus.vertx.http.HttpServer.HTTP_SERVER;

import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkus.runtime.QuarkusRuntime;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;

// TODO -
//  This is not exactly a source, but just a way to register stuff with the QuarkusRuntime, very early in the
//  startup process. Ideally we should create a specific way to call QuarkusRuntime.register. Also, this will register
//  for @QuarkusIntegrationTest
public class HttpServerFactory implements ConfigSourceFactory {
    static {
        QuarkusRuntime.registerInfo(HTTP_SERVER, HttpServer.INFO);
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        return List.of();
    }
}
