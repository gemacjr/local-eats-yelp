package com.yelpclone.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, S3 s3, Search search) {

    public record Jwt(String secret, long ttlHours) {}

    public record Cors(List<String> allowedOrigins) {}

    public record S3(String bucket, String region, String endpoint, String publicEndpoint, long presignMinutes) {
        public boolean hasEndpoint() {
            return endpoint != null && !endpoint.isBlank();
        }

        public String presignEndpoint() {
            return publicEndpoint != null && !publicEndpoint.isBlank() ? publicEndpoint : endpoint;
        }
    }

    public record Search(double defaultRadiusMeters, double maxRadiusMeters, int maxPageSize) {}
}
