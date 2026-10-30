package io.quarkus.oidc.common.runtime.config;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import io.smallrye.config.ConfigInstanceBuilder;

public abstract class OidcCommonConfigBuilder<T> {

    private static OidcCommonConfig.Tls tlsConfig(String tlsConfigurationName) {
        return ConfigInstanceBuilder.forInterface(OidcCommonConfig.Tls.class)
                .with(OidcCommonConfig.Tls::tlsConfigurationName, Optional.ofNullable(tlsConfigurationName))
                .build();
    }

    private static OidcCommonConfig.Proxy proxyConfig(Optional<String> proxyConfigurationName) {
        return ConfigInstanceBuilder.forInterface(OidcCommonConfig.Proxy.class)
                .with(OidcCommonConfig.Proxy::proxyConfigurationName, proxyConfigurationName)
                .build();
    }

    protected static <E> Optional<List<E>> optionalListOf(Collection<E> values) {
        return values.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(values));
    }

    protected static <I extends OidcCommonConfig> ConfigInstanceBuilder<I> commonConfig(Class<I> type,
            OidcCommonConfigBuilder<?> builder) {
        return ConfigInstanceBuilder.forInterface(type)
                .with(OidcCommonConfig::authServerUrl, builder.authServerUrl)
                .with(OidcCommonConfig::discoveryPath, builder.discoveryPath)
                .with(OidcCommonConfig::discoveryEnabled, builder.discoveryEnabled)
                .with(OidcCommonConfig::registrationPath, builder.registrationPath)
                .with(OidcCommonConfig::connectionDelay, builder.connectionDelay)
                .with(OidcCommonConfig::connectionRetryCount, builder.connectionRetryCount)
                .with(OidcCommonConfig::connectionTimeout, builder.connectionTimeout)
                .with(OidcCommonConfig::useBlockingDnsLookup, builder.useBlockingDnsLookup)
                .with(OidcCommonConfig::maxPoolSize, builder.maxPoolSize)
                .with(OidcCommonConfig::followRedirects, builder.followRedirects)
                .with(OidcCommonConfig::proxy, proxyConfig(builder.proxyConfigurationName))
                .with(OidcCommonConfig::tls, builder.tls);
    }

    private Optional<String> authServerUrl;
    private String discoveryPath;
    private Optional<Boolean> discoveryEnabled;
    private Optional<String> registrationPath;
    private Optional<Duration> connectionDelay;
    private int connectionRetryCount;
    private Duration connectionTimeout;
    private boolean useBlockingDnsLookup;
    private OptionalInt maxPoolSize;
    private boolean followRedirects;
    private Optional<String> proxyConfigurationName;
    private OidcCommonConfig.Tls tls;

    protected OidcCommonConfigBuilder(OidcCommonConfig oidcCommonConfig) {
        this.authServerUrl = oidcCommonConfig.authServerUrl();
        this.discoveryPath = oidcCommonConfig.discoveryPath();
        this.discoveryEnabled = oidcCommonConfig.discoveryEnabled();
        this.registrationPath = oidcCommonConfig.registrationPath();
        this.connectionDelay = oidcCommonConfig.connectionDelay();
        this.connectionRetryCount = oidcCommonConfig.connectionRetryCount();
        this.connectionTimeout = oidcCommonConfig.connectionTimeout();
        this.useBlockingDnsLookup = oidcCommonConfig.useBlockingDnsLookup();
        this.maxPoolSize = oidcCommonConfig.maxPoolSize();
        this.followRedirects = oidcCommonConfig.followRedirects();
        this.proxyConfigurationName = oidcCommonConfig.proxy().proxyConfigurationName();
        this.tls = oidcCommonConfig.tls();
    }

    protected abstract T getBuilder();

    /**
     * @return current {@link OidcCommonConfig#authServerUrl()}
     */
    protected Optional<String> getAuthServerUrl() {
        return authServerUrl;
    }

    /**
     * @return current {@link OidcCommonConfig#discoveryEnabled()}
     */
    protected Optional<Boolean> getDiscoveryEnabled() {
        return discoveryEnabled;
    }

    /**
     * @param authServerUrl {@link OidcCommonConfig#authServerUrl()}
     * @return T builder
     */
    public T authServerUrl(String authServerUrl) {
        this.authServerUrl = Optional.ofNullable(authServerUrl);
        return getBuilder();
    }

    /**
     * @param discoveryPath {@link OidcCommonConfig#discoveryPath()}
     * @return T builder
     */
    public T discoveryPath(String discoveryPath) {
        this.discoveryPath = discoveryPath;
        return getBuilder();
    }

    /**
     * @param discoveryEnabled {@link OidcCommonConfig#discoveryEnabled()}
     * @return T builder
     */
    public T discoveryEnabled(boolean discoveryEnabled) {
        this.discoveryEnabled = Optional.of(discoveryEnabled);
        return getBuilder();
    }

    /**
     * @param registrationPath {@link OidcCommonConfig#registrationPath()}
     * @return T builder
     */
    public T registrationPath(String registrationPath) {
        this.registrationPath = Optional.ofNullable(registrationPath);
        return getBuilder();
    }

    /**
     * @param connectionDelay {@link OidcCommonConfig#connectionDelay()}
     * @return T builder
     */
    public T connectionDelay(Duration connectionDelay) {
        this.connectionDelay = Optional.ofNullable(connectionDelay);
        return getBuilder();
    }

    /**
     * @param connectionRetryCount {@link OidcCommonConfig#connectionRetryCount()}
     * @return T builder
     */
    public T connectionRetryCount(int connectionRetryCount) {
        this.connectionRetryCount = connectionRetryCount;
        return getBuilder();
    }

    /**
     * @param connectionTimeout {@link OidcCommonConfig#connectionTimeout()}
     * @return T builder
     */
    public T connectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return getBuilder();
    }

    /**
     * @param useBlockingDnsLookup {@link OidcCommonConfig#useBlockingDnsLookup()}
     * @return T builder
     */
    public T useBlockingDnsLookup(boolean useBlockingDnsLookup) {
        this.useBlockingDnsLookup = useBlockingDnsLookup;
        return getBuilder();
    }

    /**
     * @param maxPoolSize {@link OidcCommonConfig#maxPoolSize()}
     * @return T builder
     */
    public T maxPoolSize(int maxPoolSize) {
        this.maxPoolSize = OptionalInt.of(maxPoolSize);
        return getBuilder();
    }

    /**
     * @param followRedirects {@link OidcCommonConfig#followRedirects()}
     * @return T builder
     */
    public T followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return getBuilder();
    }

    /**
     * @param tlsConfigurationName {@link OidcCommonConfig.Tls#tlsConfigurationName()}
     * @return T builder
     */
    public T tlsConfigurationName(String tlsConfigurationName) {
        this.tls = tlsConfig(tlsConfigurationName);
        return getBuilder();
    }

    /**
     * @param proxyConfigurationName {@link OidcCommonConfig.Proxy#proxyConfigurationName()}
     * @return T builder
     */
    public T proxyConfigurationName(String proxyConfigurationName) {
        this.proxyConfigurationName = Optional.ofNullable(proxyConfigurationName);
        return getBuilder();
    }
}
