package io.quarkus.test.common;

import io.quarkus.bootstrap.runtime.QuarkusRuntime;
import io.quarkus.bootstrap.runtime.QuarkusRuntime.RuntimeKey;

// TODO - Maybe we can get rid of this and use QuarkusRuntime directly
public class ListeningAddress {
    private final Integer port;
    private final String protocol;

    public ListeningAddress(Integer port, String protocol) {
        this.port = port;
        this.protocol = protocol;
    }

    public Integer getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public boolean isSsl() {
        return "https".equals(protocol);
    }

    public void register(QuarkusRuntime quarkusRuntime) {
        quarkusRuntime.register(PORT, port);
        quarkusRuntime.register(PROTOCOL, protocol);
        quarkusRuntime.register(isSsl() ? HTTPS_PORT : HTTP_PORT, port);
    }

    public static final RuntimeKey<Integer> PORT = RuntimeKey.intKey("port");
    public static final RuntimeKey<String> PROTOCOL = RuntimeKey.key("protocol");
    // Compatibility with Config and io.quarkus.vertx.http.HttpServer
    public static final RuntimeKey<Integer> HTTP_PORT = RuntimeKey.intKey("quarkus.http.port");
    public static final RuntimeKey<Integer> HTTPS_PORT = RuntimeKey.intKey("quarkus.http.ssl-port");
}
