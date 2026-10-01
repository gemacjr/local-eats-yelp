package com.yelpclone;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** Boots the full app against a real PostGIS container (singleton, shared across test classes). */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
        DockerImageName.parse("imresamu/postgis:16-3.5").asCompatibleSubstituteFor("postgres"));

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void s3(DynamicPropertyRegistry registry) {
        // Photo endpoints are not exercised here; a dummy endpoint keeps the S3 client offline-safe.
        registry.add("app.s3.endpoint", () -> "http://localhost:4566");
    }
}
