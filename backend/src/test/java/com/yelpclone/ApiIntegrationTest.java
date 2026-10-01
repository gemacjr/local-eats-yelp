package com.yelpclone;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ApiIntegrationTest extends IntegrationTestBase {

    @Autowired
    TestRestTemplate rest;

    // ---------- search ----------

    @Test
    void searchByTextFindsBusiness() {
        JsonNode body = get("/api/businesses?query=burrito");
        assertThat(names(body)).contains("La Taqueria");
    }

    @Test
    void partialNameMatchesViaTrigramFallback() {
        assertThat(names(get("/api/businesses?query=taq"))).contains("La Taqueria");
    }

    @Test
    void searchByPointAndRadiusIsSortedByDistance() {
        // Standing at Tartine; 1km radius should include Mission spots but nothing in NYC.
        JsonNode body = get("/api/businesses?lat=37.7614&lng=-122.4241&radius=1000");
        assertThat(names(body)).contains("Tartine Bakery", "Ritual Coffee Roasters").doesNotContain("Joe's Pizza");
        assertThat(body.at("/items/0/name").asText()).isEqualTo("Tartine Bakery");
        assertThat(body.at("/items/0/distanceMeters").asDouble()).isLessThan(5);
    }

    @Test
    void searchByNamedLocationUsesPrecomputedPolygons() {
        JsonNode body = get("/api/businesses?location=sf-north-beach");
        assertThat(names(body)).contains("Tony's Pizza Napoletana", "Caffe Trieste")
            .doesNotContain("La Taqueria", "Joe's Pizza");
    }

    @Test
    void searchByCategoryAndLocation() {
        JsonNode body = get("/api/businesses?category=Pizza&location=new-york");
        assertThat(names(body)).containsExactly("Joe's Pizza");
    }

    @Test
    void paginationReportsHasMore() {
        JsonNode first = get("/api/businesses?size=5&page=0");
        assertThat(first.get("items")).hasSize(5);
        assertThat(first.get("hasMore").asBoolean()).isTrue();
    }

    // ---------- reviews ----------

    @Test
    void reviewUpdatesRatingAndDuplicateIsRejected() {
        long id = createBusiness("Review Target " + UUID.randomUUID());
        String token = register();

        ResponseEntity<JsonNode> first = post("/api/businesses/" + id + "/reviews", Map.of("rating", 4, "text", "Great"), token);
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<JsonNode> dup = post("/api/businesses/" + id + "/reviews", Map.of("rating", 1), token);
        assertThat(dup.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        post("/api/businesses/" + id + "/reviews", Map.of("rating", 2), register());

        JsonNode detail = get("/api/businesses/" + id);
        assertThat(detail.get("numRatings").asInt()).isEqualTo(2);
        assertThat(detail.get("avgRating").asDouble()).isEqualTo(3.0);
        assertThat(detail.get("recentReviews")).hasSize(2);
    }

    @Test
    void concurrentReviewsAreAllCountedDespiteOptimisticLocking() throws Exception {
        long id = createBusiness("Busy Spot " + UUID.randomUUID());
        int n = 8;
        var tokens = IntStream.range(0, n).mapToObj(i -> register()).toList();
        try (ExecutorService pool = Executors.newFixedThreadPool(n)) {
            var start = new CountDownLatch(1);
            var futures = tokens.stream().map(t -> pool.submit(() -> {
                start.await();
                return post("/api/businesses/" + id + "/reviews", Map.of("rating", 5), t).getStatusCode();
            })).toList();
            start.countDown();
            for (Future<HttpStatusCode> f : futures) assertThat(f.get(30, TimeUnit.SECONDS)).isEqualTo(HttpStatus.CREATED);
        }
        JsonNode detail = get("/api/businesses/" + id);
        assertThat(detail.get("numRatings").asInt()).isEqualTo(n);
        assertThat(detail.get("avgRating").asDouble()).isEqualTo(5.0);
    }

    @Test
    void writingRequiresAuthentication() {
        ResponseEntity<JsonNode> res = post("/api/businesses/1/reviews", Map.of("rating", 5), null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void invalidRatingIsRejected() {
        ResponseEntity<JsonNode> res = post("/api/businesses/1/reviews", Map.of("rating", 6), register());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // ---------- helpers ----------

    private JsonNode get(String path) {
        ResponseEntity<JsonNode> res = rest.getForEntity(path, JsonNode.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).as(path + " -> " + res.getBody()).isTrue();
        return res.getBody();
    }

    private ResponseEntity<JsonNode> post(String path, Object body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) headers.setBearerAuth(token);
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), JsonNode.class);
    }

    private String register() {
        String email = "u" + UUID.randomUUID() + "@example.com";
        var res = post("/api/auth/register", Map.of("email", email, "displayName", "Tester", "password", "password123"), null);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return res.getBody().get("token").asText();
    }

    private long createBusiness(String name) {
        var res = post("/api/businesses", Map.of("name", name, "category", "Coffee", "address", "1 Valencia St",
            "latitude", 37.7600, "longitude", -122.4200), register());
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return res.getBody().get("id").asLong();
    }

    private static java.util.List<String> names(JsonNode page) {
        return page.get("items").findValuesAsText("name");
    }
}
