package io.quarkus.resteasy.reactive.jackson.deployment.processor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.ws.rs.Priorities;
import javax.ws.rs.core.MediaType;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.resteasy.reactive.server.util.MethodId;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.Feature;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.resteasy.reactive.common.deployment.ResourceScanningResultBuildItem;
import io.quarkus.resteasy.reactive.common.deployment.ServerDefaultProducesHandlerBuildItem;
import io.quarkus.resteasy.reactive.jackson.CustomSerialization;
import io.quarkus.resteasy.reactive.jackson.runtime.ResteasyReactiveServerJacksonRecorder;
import io.quarkus.resteasy.reactive.jackson.runtime.mappers.DefaultMismatchedInputException;
import io.quarkus.resteasy.reactive.jackson.runtime.mappers.NativeInvalidDefinitionExceptionMapper;
import io.quarkus.resteasy.reactive.jackson.runtime.serialisers.BasicServerJacksonMessageBodyWriter;
import io.quarkus.resteasy.reactive.jackson.runtime.serialisers.FullyFeaturedServerJacksonMessageBodyWriter;
import io.quarkus.resteasy.reactive.jackson.runtime.serialisers.ServerJacksonMessageBodyReader;
import io.quarkus.resteasy.reactive.jackson.runtime.serialisers.vertx.VertxJsonArrayMessageBodyReader;
import io.quarkus.resteasy.reactive.jackson.runtime.serialisers.vertx.VertxJsonArrayMessageBodyWriter;
import io.quarkus.resteasy.reactive.jackson.runtime.serialisers.vertx.VertxJsonObjectMessageBodyReader;
import io.quarkus.resteasy.reactive.jackson.runtime.serialisers.vertx.VertxJsonObjectMessageBodyWriter;
import io.quarkus.resteasy.reactive.spi.CustomExceptionMapperBuildItem;
import io.quarkus.resteasy.reactive.spi.ExceptionMapperBuildItem;
import io.quarkus.resteasy.reactive.spi.MessageBodyReaderBuildItem;
import io.quarkus.resteasy.reactive.spi.MessageBodyWriterBuildItem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class ResteasyReactiveJacksonProcessor {

    private static final DotName JSON_VIEW = DotName.createSimple(JsonView.class.getName());
    private static final DotName CUSTOM_SERIALIZATION = DotName.createSimple(CustomSerialization.class.getName());
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @BuildStep
    void feature(BuildProducer<FeatureBuildItem> feature) {
        feature.produce(new FeatureBuildItem(Feature.RESTEASY_REACTIVE_JACKSON));
    }

    @BuildStep
    ServerDefaultProducesHandlerBuildItem jsonDefault() {
        return ServerDefaultProducesHandlerBuildItem.json();
    }

    @BuildStep
    ResteasyReactiveJacksonProviderDefinedBuildItem jacksonRegistered() {
        return new ResteasyReactiveJacksonProviderDefinedBuildItem();
    }

    @BuildStep
    void additionalProviders(List<JacksonFeatureBuildItem> jacksonFeatureBuildItems,
            BuildProducer<AdditionalBeanBuildItem> additionalBean,
            BuildProducer<MessageBodyReaderBuildItem> additionalReaders,
            BuildProducer<MessageBodyWriterBuildItem> additionalWriters,
            BuildProducer<ExceptionMapperBuildItem> exceptionMappers,
            BuildProducer<CustomExceptionMapperBuildItem> customExceptionMapper) {
        boolean applicationNeedsSpecialJacksonFeatures = jacksonFeatureBuildItems.isEmpty();
        // make these beans to they can get instantiated with the Quarkus CDI configured ObjectMapper object
        additionalBean.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(ServerJacksonMessageBodyReader.class.getName())
                .addBeanClass(getJacksonMessageBodyWriter(applicationNeedsSpecialJacksonFeatures))
                .setUnremovable().build());

        additionalReaders
                .produce(new MessageBodyReaderBuildItem(ServerJacksonMessageBodyReader.class.getName(), Object.class.getName(),
                        Collections.singletonList(MediaType.APPLICATION_JSON)));
        additionalReaders
                .produce(new MessageBodyReaderBuildItem(VertxJsonArrayMessageBodyReader.class.getName(),
                        JsonArray.class.getName(),
                        Collections.singletonList(MediaType.APPLICATION_JSON)));
        additionalReaders
                .produce(new MessageBodyReaderBuildItem(VertxJsonObjectMessageBodyReader.class.getName(),
                        JsonObject.class.getName(),
                        Collections.singletonList(MediaType.APPLICATION_JSON)));
        additionalWriters
                .produce(new MessageBodyWriterBuildItem(getJacksonMessageBodyWriter(applicationNeedsSpecialJacksonFeatures),
                        Object.class.getName(),
                        Collections.singletonList(MediaType.APPLICATION_JSON)));
        additionalWriters
                .produce(new MessageBodyWriterBuildItem(VertxJsonArrayMessageBodyWriter.class.getName(),
                        JsonArray.class.getName(),
                        Collections.singletonList(MediaType.APPLICATION_JSON)));
        additionalWriters
                .produce(new MessageBodyWriterBuildItem(VertxJsonObjectMessageBodyWriter.class.getName(),
                        JsonObject.class.getName(),
                        Collections.singletonList(MediaType.APPLICATION_JSON)));

        exceptionMappers.produce(new ExceptionMapperBuildItem(DefaultMismatchedInputException.class.getName(),
                MismatchedInputException.class.getName(), Priorities.USER + 100, false));

        customExceptionMapper
                .produce(new CustomExceptionMapperBuildItem(NativeInvalidDefinitionExceptionMapper.class.getName()));
    }

    private String getJacksonMessageBodyWriter(boolean applicationNeedsSpecialJacksonFeatures) {
        return applicationNeedsSpecialJacksonFeatures ? BasicServerJacksonMessageBodyWriter.class.getName()
                : FullyFeaturedServerJacksonMessageBodyWriter.class.getName();
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep
    void handleJsonAnnotations(Optional<ResourceScanningResultBuildItem> resourceScanningResultBuildItem,
            CombinedIndexBuildItem index,
            BuildProducer<ReflectiveClassBuildItem> reflectiveClassProducer,
            BuildProducer<JacksonFeatureBuildItem> jacksonFeaturesProducer,
            ResteasyReactiveServerJacksonRecorder recorder, ShutdownContextBuildItem shutdown) {
        if (!resourceScanningResultBuildItem.isPresent()) {
            return;
        }
        Collection<ClassInfo> resourceClasses = resourceScanningResultBuildItem.get().getResult().getScannedResources()
                .values();
        Set<JacksonFeatureBuildItem.Feature> jacksonFeatures = new HashSet<>();
        for (ClassInfo resourceClass : resourceClasses) {
            if (resourceClass.annotations().containsKey(JSON_VIEW)) {
                jacksonFeatures.add(JacksonFeatureBuildItem.Feature.JSON_VIEW);
                for (AnnotationInstance instance : resourceClass.annotations().get(JSON_VIEW)) {
                    AnnotationValue annotationValue = instance.value();
                    if (annotationValue == null) {
                        continue;
                    }
                    if (instance.target().kind() != AnnotationTarget.Kind.METHOD) {
                        continue;
                    }
                    Type[] jsonViews = annotationValue.asClassArray();
                    if ((jsonViews == null) || (jsonViews.length == 0)) {
                        continue;
                    }
                    recorder.recordJsonView(getMethodId(instance.target().asMethod()), jsonViews[0].name().toString());
                }
            }
            if (resourceClass.annotations().containsKey(CUSTOM_SERIALIZATION)) {
                jacksonFeatures.add(JacksonFeatureBuildItem.Feature.CUSTOM_SERIALIZATION);
                for (AnnotationInstance instance : resourceClass.annotations().get(CUSTOM_SERIALIZATION)) {
                    AnnotationValue annotationValue = instance.value();
                    if (annotationValue == null) {
                        continue;
                    }
                    if (instance.target().kind() != AnnotationTarget.Kind.METHOD) {
                        continue;
                    }
                    Type biFunctionType = annotationValue.asClass();
                    if (biFunctionType == null) {
                        continue;
                    }
                    ClassInfo biFunctionClassInfo = index.getIndex().getClassByName(biFunctionType.name());
                    if (biFunctionClassInfo == null) {
                        // be lenient
                    } else {
                        if (!biFunctionClassInfo.hasNoArgsConstructor()) {
                            throw new IllegalArgumentException(
                                    "Class '" + biFunctionClassInfo.name() + "' must contain a no-args constructor");
                        }
                    }
                    reflectiveClassProducer.produce(
                            new ReflectiveClassBuildItem(true, false, false, biFunctionType.name().toString()));
                    recorder.recordCustomSerialization(getMethodId(instance.target().asMethod()),
                            biFunctionType.name().toString());
                }
            }
        }
        if (!jacksonFeatures.isEmpty()) {
            for (JacksonFeatureBuildItem.Feature jacksonFeature : jacksonFeatures) {
                jacksonFeaturesProducer.produce(new JacksonFeatureBuildItem(jacksonFeature));
            }
            recorder.configureShutdown(shutdown);
        }
    }

    private String getMethodId(MethodInfo methodInfo) {
        List<String> parameterClassNames = new ArrayList<>(methodInfo.parameters().size());
        for (Type parameter : methodInfo.parameters()) {
            parameterClassNames.add(parameter.name().toString());
        }
        return MethodId.get(methodInfo.name(), methodInfo.declaringClass().name().toString(),
                parameterClassNames.toArray(EMPTY_STRING_ARRAY));
    }
}
