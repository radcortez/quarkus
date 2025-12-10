package io.quarkus.test.junit;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import io.quarkus.runtime.QuarkusRuntime;

public class QuarkusRuntimeInfoParameterResolver implements ParameterResolver {
    static final QuarkusRuntimeInfoParameterResolver INSTANCE = new QuarkusRuntimeInfoParameterResolver();

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return QuarkusRuntime.getInfo(parameterContext.getParameter().getType()) != null;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Store store = extensionContext.getStore(Namespace.GLOBAL);
        QuarkusTestExtensionState state = store.get(QuarkusTestExtensionState.class.getName(), QuarkusTestExtensionState.class);
        Map<String, Object> info = new HashMap<>();
        state.getListeningAddress().ifPresent(la -> {
            info.put(la.isSsl() ? "ssl-port" : "http-port", la.getPort());
        });
        return QuarkusRuntime.getInfo(parameterContext.getParameter().getType()).get(info);
    }
}
