package io.quarkus.vertx.http;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigSource;

import io.quarkus.runtime.QuarkusRuntime;
import io.quarkus.runtime.QuarkusRuntime.Info;
import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;

// TODO -
//  This is not exactly a source, but just a way to register stuff with the QuarkusRuntime, very early in the
//  startup process. Ideally we should create a specific way to call QuarkusRuntime.register. Also, this will register
//  for @QuarkusIntegrationTest
public class HttpServerFactory implements ConfigSourceFactory {
    static {
        QuarkusRuntime.register(HttpServer.class, new Info<>() {
            // TODO - This is querying the values from Config (as it is today), but replace with the RuntimeValues API when available
            @Override
            public HttpServer get() {
                return new HttpServer() {
                    @Override
                    public int getPort() {
                        return ConfigProvider.getConfig().getOptionalValue("quarkus.http.port", Integer.class).orElse(-1);
                    }

                    @Override
                    public int getSecurePort() {
                        return ConfigProvider.getConfig().getOptionalValue("quarkus.http.ssl-port", Integer.class).orElse(-1);
                    }

                    @Override
                    public int getManagementPort() {
                        return ConfigProvider.getConfig().getOptionalValue("quarkus.management.port", Integer.class).orElse(-1);
                    }
                };
            }

            @Override
            public HttpServer get(Map<String, Object> properties) {
                return new HttpServer() {
                    @Override
                    public int getPort() {
                        return (int) properties.getOrDefault("http-port", -1);
                    }

                    @Override
                    public int getSecurePort() {
                        return (int) properties.getOrDefault("ssl-port", -1);
                    }

                    @Override
                    public int getManagementPort() {
                        return (int) properties.getOrDefault("management-port", -1);
                    }
                };
            }
        });
    }

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        return List.of();
    }
}
