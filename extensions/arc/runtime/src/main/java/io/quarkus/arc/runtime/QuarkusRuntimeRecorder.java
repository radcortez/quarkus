package io.quarkus.arc.runtime;

import java.util.function.Supplier;

import io.quarkus.bootstrap.runtime.QuarkusRuntime;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class QuarkusRuntimeRecorder {
    private final RuntimeValue<QuarkusRuntime> quarkusRuntime;

    public QuarkusRuntimeRecorder(RuntimeValue<QuarkusRuntime> quarkusRuntime) {
        this.quarkusRuntime = quarkusRuntime;
    }

    public Supplier<QuarkusRuntime> quarkusRuntime() {
        return new Supplier<>() {
            @Override
            public QuarkusRuntime get() {
                // TODO - Is this the right place to register this? To be able to inject this in IT as well?
                //   Also, is this only called once?
                return quarkusRuntime.getValue();
            }
        };
    }
}
