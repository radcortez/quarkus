package io.quarkus.vertx.http.deployment;

import io.quarkus.builder.item.EmptyBuildItem;

/**
 * Marker produced once the HTTP routers have been finalized. The server binds its sockets after this point, before
 * the application starts.
 */
public final class RouterFinalizedBuildItem extends EmptyBuildItem {
}
