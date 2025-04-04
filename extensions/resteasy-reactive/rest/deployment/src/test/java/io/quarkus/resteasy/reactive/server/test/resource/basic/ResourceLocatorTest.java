package io.quarkus.resteasy.reactive.server.test.resource.basic;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.function.Supplier;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.CorsPreflightResource;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorAbstractAnnotationFreeResource;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorAnnotationFreeSubResource;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorBaseResource;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorCollectionResource;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorDirectory;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorQueueReceiver;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorReceiver;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorRootInterface;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorSubInterface;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorSubresource;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorSubresource2;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorSubresource3;
import io.quarkus.resteasy.reactive.server.test.resource.basic.resource.ResourceLocatorSubresource3Interface;
import io.quarkus.resteasy.reactive.server.test.simple.PortProviderUtil;
import io.quarkus.test.QuarkusUnitTest;

/**
 * @tpSubChapter Resource
 * @tpChapter Integration tests
 * @tpTestCaseDetails Tests path encoding
 * @tpSince RESTEasy 3.0.20
 */
@DisplayName("Resource Locator Test")
public class ResourceLocatorTest {

    static Client client;

    @BeforeAll
    public static void setup() {
        client = ClientBuilder.newClient();
    }

    @AfterAll
    public static void close() {
        client.close();
    }

    @RegisterExtension
    static QuarkusUnitTest testExtension = new QuarkusUnitTest()
            .overrideConfigKey("quarkus.http.root-path", "/app")
            .setArchiveProducer(new Supplier<>() {
                @Override
                public JavaArchive get() {
                    JavaArchive war = ShrinkWrap.create(JavaArchive.class);
                    war.addClass(ResourceLocatorQueueReceiver.class).addClass(ResourceLocatorReceiver.class)
                            .addClass(ResourceLocatorRootInterface.class).addClass(ResourceLocatorSubInterface.class)
                            .addClass(ResourceLocatorSubresource3Interface.class).addClass(CorsPreflightResource.class);
                    war.addClasses(PortProviderUtil.class, ResourceLocatorAbstractAnnotationFreeResource.class,
                            ResourceLocatorAnnotationFreeSubResource.class, ResourceLocatorBaseResource.class,
                            ResourceLocatorCollectionResource.class, ResourceLocatorDirectory.class,
                            ResourceLocatorSubresource.class, ResourceLocatorSubresource2.class,
                            ResourceLocatorSubresource3.class);
                    return war;
                }
            });

    private String generateURL(String path) {
        return PortProviderUtil.generateURL(path, ResourceLocatorTest.class.getSimpleName());
    }

    /**
     * @tpTestDetails Resource locator returns proxied resource.
     * @tpSince RESTEasy 3.0.20
     */
    @Test
    @DisplayName("Test Proxied Subresource")
    public void testProxiedSubresource() throws Exception {
        WebTarget target = client.target(generateURL("/proxy/3"));
        Response res = target.queryParam("foo", "1.2").queryParam("foo", "1.3").request().get();
        Assertions.assertEquals(200, res.getStatus());
        res.close();
    }

    /**
     * @tpTestDetails 1) Resource locator returns resource; 2) Resource locator returns resource locator.
     * @tpSince RESTEasy 3.0.20
     */
    @Test
    @DisplayName("Test Subresource")
    public void testSubresource() throws Exception {
        {
            Response response = client.target(generateURL("/base/1/resources")).request().get();
            Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            Assertions.assertEquals(ResourceLocatorSubresource.class.getName(), response.readEntity(String.class));
        }
        {
            Response response = client.target(generateURL("/base/1/resources/subresource2/stuff/2/bar")).request().get();
            Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            Assertions.assertEquals(ResourceLocatorSubresource2.class.getName() + "-2", response.readEntity(String.class));
        }
    }

    /**
     * @tpTestDetails Return custom handler for HTTP OPTIONS method in subresource redirection. The
     *                {@link CorsPreflightResource} instance should be returned
     */
    @Test
    @DisplayName("Test custom HTTP OPTIONS handler in subresource")
    public void testOptionsMethodInSubresource() {
        try (Response response = client.target(generateURL("/sub3/something/resources/test-options-method")).request()
                .options()) {
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

            var customHeader = response.getHeaderString(CorsPreflightResource.TEST_PREFLIGHT_HEADER);
            assertThat(customHeader, notNullValue());
            assertThat(customHeader, is("test"));

            var allowHeader = response.getHeaderString("Allow");
            assertThat(allowHeader, notNullValue());
            assertThat(Arrays.asList(allowHeader.split(", ")),
                    containsInAnyOrder(HttpMethod.GET, HttpMethod.POST, HttpMethod.OPTIONS, HttpMethod.HEAD));
        }
    }

    /**
     * @tpTestDetails Custom handler for HTTP OPTIONS method in subresource.
     */
    @Test
    @DisplayName("Test custom explicit HTTP OPTIONS handler in subresource")
    public void testOptionsMethodExplicitInSubresource() {
        try (Response response = client.target(generateURL("/sub3/something/resources/test-options-method-explicit")).request()
                .options()) {
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));

            var customHeader = response.getHeaderString(ResourceLocatorSubresource2.TEST_PREFLIGHT_HEADER);
            assertThat(customHeader, notNullValue());
            assertThat(customHeader, is("test"));

            var allowHeader = response.getHeaderString("Allow");
            assertThat(allowHeader, notNullValue());
            assertThat(Arrays.asList(allowHeader.split(", ")), containsInAnyOrder(HttpMethod.GET));
        }
    }

    /**
     * @tpTestDetails Two matching methods, one a resource locator, the other a resource method.
     * @tpSince RESTEasy 3.0.20
     */
    @Test
    @DisplayName("Test Same Uri")
    public void testSameUri() throws Exception {
        Response response = client.target(generateURL("/directory/receivers/1")).request().delete();
        Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        Assertions.assertEquals(ResourceLocatorDirectory.class.getName(), response.readEntity(String.class));
    }

    /**
     * @tpTestDetails Locator returns resource which inherits annotations from an interface.
     * @tpSince RESTEasy 3.0.20
     */
    @Test
    @DisplayName("Test Annotation Free Subresource")
    public void testAnnotationFreeSubresource() throws Exception {
        {
            Response response = client.target(generateURL("/collection/annotation_free_subresource")).request().get();
            Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            Assertions.assertEquals(response.readEntity(String.class), "got");
            Assertions.assertNotNull(response.getHeaderString("Content-Type"));
            Assertions.assertEquals("text/plain;charset=UTF-8",
                    response.getHeaderString("Content-Type"));
        }
        {
            Builder request = client.target(generateURL("/collection/annotation_free_subresource")).request();
            Response response = request.post(Entity.entity("hello!".getBytes(), MediaType.TEXT_PLAIN));
            Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            Assertions.assertEquals("posted: hello!", response.readEntity(String.class));
        }
    }

    @Test
    @DisplayName("Test @BeanParam annotation in Subresources")
    public void testBeanParamsInSubresource() {
        given().get("/sub3/first/resources/subresource3?value=second")
                .then()
                .body(is("first and second"));
    }

    @Test
    public void testRoot() throws Exception {
        given().get().then().body(is("[app, ]"));
    }
}
