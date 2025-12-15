package io.quarkus.test.junit;

import static io.quarkus.runtime.QuarkusRuntime.RuntimeKey.key;

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
        return QuarkusRuntime.containsKey(key(parameterContext.getParameter().getType()));
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        Store store = extensionContext.getStore(Namespace.GLOBAL);
        QuarkusTestExtensionState state = store.get(QuarkusTestExtensionState.class.getName(), QuarkusTestExtensionState.class);
        if (state == null) {
            throw new ParameterResolutionException("Could not retrieve parameter: " + parameterContext.getParameter());
        }
        Map<String, Object> info = new HashMap<>();
        state.getListeningAddress().ifPresent(la -> {
            info.put(la.isSsl() ? "ssl-port" : "http-port", la.getPort());
        });

        return QuarkusRuntime.getInfo(key(parameterContext.getParameter().getType()), info);
    }
}
