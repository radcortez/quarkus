package io.quarkus.deployment.builditem;

import io.quarkus.builder.item.EmptyBuildItem;

/**
 * Marker used by Build Steps that consume runtime configuration to ensure that they run after the runtime config has been set
 * up.
 */
@Deprecated(forRemoval = true, since = "3.31")
public final class RuntimeConfigSetupCompleteBuildItem extends EmptyBuildItem {
}
