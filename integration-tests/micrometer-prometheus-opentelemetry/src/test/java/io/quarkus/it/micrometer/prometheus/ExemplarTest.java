package io.quarkus.it.micrometer.prometheus;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.when;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 * See Micrometer Guide
 */
@QuarkusTest
@TestProfile(OtelOnProfile.class)
public class ExemplarTest {

    @Test
    void testExemplar() {
        when().get("/example/prime/257").then().statusCode(200);
        when().get("/example/prime/7919").then().statusCode(200);

        String metricMatch = "http_server_requests_seconds_count{dummy=\"value\",env=\"test\"," +
                "env2=\"test\",foo=\"UNSET\",foo_response=\"UNSET\",method=\"GET\",outcome=\"SUCCESS\"," +
                "registry=\"prometheus\",status=\"200\",uri=\"/example/prime/{number}\"} 2 # {span_id=\"";

        await().atMost(5, SECONDS).untilAsserted(() -> {
            String body = get("/q/metrics").then().extract().asString();
            assertTrue(body.contains(metricMatch), body);
        });
    }
}
