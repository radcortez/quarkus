package io.quarkus.vertx.http;

/**
 * Represent the actual runtime values of the Quarkus HTTP Server.
 */
// TODO - Ideally we should store the SocketAddress, but Vertx only returns the port.
public interface HttpServer {
    /**
     * Return the http port that Quarkus is listening on.
     *
     * @return the port or <code>-1</code> if Quarkus is not set to listen to the http port.
     */
    int getPort();

    /**
     * Return the https port that Quarkus is listening on.
     *
     * @return the port or <code>-1</code> if Quarkus is not set to listen to the https port.
     */
    int getSecurePort();

    /**
     * Return the management http port that Quarkus is listening on.
     *
     * @return the port or <code>-1</code> if Quarkus is not set to listen to the management http port.
     */
    int getManagementPort();
}
