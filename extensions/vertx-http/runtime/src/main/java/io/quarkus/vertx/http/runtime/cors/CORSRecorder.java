package io.quarkus.vertx.http.runtime.cors;

import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.vertx.http.runtime.VertxHttpConfig;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class CORSRecorder {
    final VertxHttpConfig configuration;

    public CORSRecorder(VertxHttpConfig configuration) {
        this.configuration = configuration;
    }

    public Handler<RoutingContext> corsHandler() {
        if (configuration.corsEnabled()) {
            return new CORSFilter(configuration.cors());
        }
        return null;
    }

}
