package io.quarkus.vertx.http.runtime;

import jakarta.enterprise.inject.Vetoed;

import io.quarkus.runtime.QuarkusRuntime;
import io.quarkus.runtime.QuarkusRuntime.RuntimeKey;

@Vetoed
public class QuarkusWebServer {
    private final static QuarkusWebServer INSTANCE = new QuarkusWebServer();

    public static final RuntimeKey<Integer> HTTP_PORT = RuntimeKey.intKey("quarkus.http.port");
    public static final RuntimeKey<Integer> HTTPS_PORT = RuntimeKey.intKey("quarkus.https.port");

    public int getPort() {
        return QuarkusRuntime.instance().get(HTTP_PORT);
    }

    void setPort(int port) {
        QuarkusRuntime.instance().register(HTTP_PORT, port);
    }

    public int getSecurePort() {
        return QuarkusRuntime.instance().get(HTTPS_PORT);
    }

    void setSecurePort(int port) {
        QuarkusRuntime.instance().register(HTTPS_PORT, port);
    }

    public static QuarkusWebServer instance() {
        return INSTANCE;
    }
}
