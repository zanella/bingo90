package zane.raf.bingo_90;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class Bingo90Test {
    @Test
    public void testStripEndpoint() throws Exception {
        final var response = given()
            .when().get("/bingo90/api/strip")
            .then()
            .statusCode(200)
            .extract()
            .response();

        final var strip = new ObjectMapper().readValue(response.body().print(), Strip.class);

        assertEquals(6, strip.tickets().size());
    }

    @Test
    public void testIndexTemplate() {
        given()
            .when().get("/bingo90")
            .then()
            .statusCode(200);
    }
}
