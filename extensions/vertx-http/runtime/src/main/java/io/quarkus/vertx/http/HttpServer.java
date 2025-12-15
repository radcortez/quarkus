package io.quarkus.vertx.http;

import java.util.Map;

import io.quarkus.runtime.QuarkusRuntime;
import io.quarkus.runtime.QuarkusRuntime.Info;
import io.quarkus.runtime.QuarkusRuntime.RuntimeKey;

/**
 * Represent the actual runtime values of the Quarkus HTTP Server.
 */
// TODO - Ideally we should store the SocketAddress, but Vertx only returns the port.
public interface HttpServer {
    RuntimeKey<Integer> HTTP_PORT = RuntimeKey.intKey("quarkus.http.port");
    RuntimeKey<Integer> HTTP_TEST_PORT = RuntimeKey.intKey("quarkus.http.test-port");
    RuntimeKey<Integer> HTTPS_PORT = RuntimeKey.intKey("quarkus.http.ssl-port");
    RuntimeKey<Integer> HTTPS_TEST_PORT = RuntimeKey.intKey("quarkus.http.test-ssl-port");
    RuntimeKey<Integer> MANAGEMENT_PORT = RuntimeKey.intKey("quarkus.management.port");
    RuntimeKey<Integer> MANAGEMENT_TEST_PORT = RuntimeKey.intKey("quarkus.management.test-port");

    RuntimeKey<HttpServer> HTTP_SERVER = RuntimeKey.key(HttpServer.class);

    /**
     * Return the http port that Quarkus is listening on.
     *
     * @return the port or <code>-1</code> if Quarkus is not set to listen to the http port.
     */
    int getPort();

    /**
     * Return the https port that Quarkus is listening on.
     *
     * @return the port or <code>-1</code> if Quarkus is not set to listen to the https port.
     */
    int getSecurePort();

    /**
     * Return the management http port that Quarkus is listening on.
     *
     * @return the port or <code>-1</code> if Quarkus is not set to listen to the management http port.
     */
    int getManagementPort();

    /**
     * The {@link Info} implementation for {@link HttpServer}. Construct instances of {@link HttpServer} with
     * {@link QuarkusRuntime} values or with a custom Map.
     */
    Info<HttpServer> INFO = new Info<>() {
        private final HttpServer HTTP_SERVER = new HttpServer() {
            @Override
            public int getPort() {
                return QuarkusRuntime.getOrDefault(HTTP_PORT, -1);
            }

            @Override
            public int getSecurePort() {
                return QuarkusRuntime.getOrDefault(HTTPS_PORT, -1);
            }

            @Override
            public int getManagementPort() {
                return QuarkusRuntime.getOrDefault(MANAGEMENT_PORT, -1);
            }
        };

        @Override
        public HttpServer get() {
            return HTTP_SERVER;
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
    };
}
