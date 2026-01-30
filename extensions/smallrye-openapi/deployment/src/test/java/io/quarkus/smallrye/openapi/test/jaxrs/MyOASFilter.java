package io.quarkus.smallrye.openapi.test.jaxrs;

import java.util.Optional;

import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;

import io.smallrye.config.Config;

/**
 * Filter to add custom elements
 */
public class MyOASFilter implements OASFilter {

    final Config config = Config.get();

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        Optional<String> maybeVersion = config.getOptionalValue("my.openapi.version", String.class);
        String version = maybeVersion.orElse("3.0.3");
        openAPI.setOpenapi(version);

        // Below is to test runtime filters that use CDI
        CDI.current().getBeanManager().createInstance();
    }

}
