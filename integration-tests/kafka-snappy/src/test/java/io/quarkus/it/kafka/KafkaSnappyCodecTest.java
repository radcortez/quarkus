package io.quarkus.it.kafka;

import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.mapper.ObjectMapperType;

@WithTestResource(value = KafkaTestResource.class, restrictToAnnotatedClass = false)
@QuarkusTest
public class KafkaSnappyCodecTest {

    @BeforeAll
    public static void configureMapper() {
        // We have JSON-B and Jackson around, we want to ensure REST Assured uses Jackson and not JSON-B
        RestAssured.config = RestAssured.config.objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                .defaultObjectMapperType(ObjectMapperType.JACKSON_2));
    }

    @Test
    public void testCustomCodec() {
        RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body("{\"kind\":\"rabbit\", \"name\":\"neo\"}")
                .post("/codecs/pets");

        RestAssured
                .given()
                .header("Accept", "application/json")
                .get("/codecs/pets")
                .then()
                .body("kind", is("rabbit"))
                .body("name", is("neo"));

    }

    @Test
    public void testJsonbCodec() {
        RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body("{\"name\":\"kate\", \"id\":\"1234\"}")
                .post("/codecs/persons");

        RestAssured
                .given()
                .header("Accept", "application/json")
                .get("/codecs/persons")
                .then()
                .body("name", is("kate"))
                .body("id", is(1234));

    }

    @Test
    public void testJacksonCodec() {
        RestAssured
                .given()
                .header("Content-Type", "application/json")
                .body("{\"title\":\"Inception\", \"year\":\"2010\"}")
                .post("/codecs/movies");

        RestAssured
                .given()
                .header("Accept", "application/json")
                .get("/codecs/movies")
                .then()
                .body("title", is("Inception"))
                .body("year", is(2010));

    }

}
