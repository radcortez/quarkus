package io.quarkus.test.junit;

import static io.quarkus.bootstrap.runtime.QuarkusRuntime.RuntimeKey.key;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import io.quarkus.bootstrap.runtime.QuarkusRuntime;

public class QuarkusRuntimeInfoParameterResolver implements ParameterResolver {
    static final QuarkusRuntimeInfoParameterResolver INSTANCE = new QuarkusRuntimeInfoParameterResolver();

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        QuarkusRuntime quarkusRuntime = getQuarkusRuntime(parameterContext, extensionContext);
        return quarkusRuntime.containsKey(key(parameterContext.getParameter().getType()));
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        QuarkusRuntime quarkusRuntime = getQuarkusRuntime(parameterContext, extensionContext);
        return quarkusRuntime.get(key(parameterContext.getParameter().getType()));
    }

    private static QuarkusRuntime getQuarkusRuntime(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Store store = extensionContext.getStore(Namespace.GLOBAL);
        QuarkusTestExtensionState state = store.get(QuarkusTestExtensionState.class.getName(), QuarkusTestExtensionState.class);
        if (state == null) {
            throw new ParameterResolutionException("Could not retrieve parameter: " + parameterContext.getParameter());
        }
        return state.getQuarkusRuntime();
    }
}
