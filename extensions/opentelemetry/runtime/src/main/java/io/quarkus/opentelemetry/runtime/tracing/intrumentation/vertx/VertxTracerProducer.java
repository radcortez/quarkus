package io.quarkus.opentelemetry.runtime.tracing.intrumentation.vertx;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.opentelemetry.api.OpenTelemetry;
import io.quarkus.arc.DefaultBean;
import io.vertx.core.spi.tracing.VertxTracer;

@Singleton
public class VertxTracerProducer {
    @Produces
    @Singleton
    @DefaultBean
    VertxTracer vertxTracer(OpenTelemetry openTelemetry) {
        List<InstrumenterVertxTracer<?, ?>> instrumenterVertxTracers = new ArrayList<>();
        instrumenterVertxTracers.add(new HttpInstrumenterVertxTracer(openTelemetry));
        instrumenterVertxTracers.add(new EventBusInstrumenterVertxTracer(openTelemetry));
        // TODO - Selectively register this in the recorder if the SQL Client is available.
        instrumenterVertxTracers.add(new SqlClientInstrumenterVertxTracer(openTelemetry));
        return new OpenTelemetryVertxTracer(instrumenterVertxTracers);
    }
}
