package io.quarkus.arc.deployment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

import io.quarkus.arc.runtime.QuarkusRuntimeRecorder;
import io.quarkus.bootstrap.runtime.QuarkusRuntime;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;

class QuarkusRuntimeProcessor {
    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void httpServer(
            QuarkusRuntimeRecorder recorder,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        SyntheticBeanBuildItem.ExtendedBeanConfigurator configurator = SyntheticBeanBuildItem
                .configure(QuarkusRuntime.class)
                .startup()
                .setRuntimeInit()
                .unremovable()
                .supplier(recorder.quarkusRuntime())
                .scope(ApplicationScoped.class)
                .addQualifier(Default.class);

        syntheticBeans.produce(configurator.done());
    }
}
