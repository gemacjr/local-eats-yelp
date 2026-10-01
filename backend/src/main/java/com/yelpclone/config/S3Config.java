package com.yelpclone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * S3 clients. On AWS the default credential chain (ECS task role) and real endpoints are used.
 * Locally, S3_ENDPOINT points at LocalStack, which needs path-style addressing and accepts dummy credentials.
 */
@Configuration
public class S3Config {

    private static final StaticCredentialsProvider LOCALSTACK_CREDENTIALS =
        StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));

    @Bean
    S3Client s3Client(AppProperties props) {
        var s3 = props.s3();
        var builder = S3Client.builder().region(Region.of(s3.region()));
        if (s3.hasEndpoint()) {
            builder.endpointOverride(URI.create(s3.endpoint())).forcePathStyle(true)
                   .credentialsProvider(LOCALSTACK_CREDENTIALS);
        }
        return builder.build();
    }

    @Bean
    S3Presigner s3Presigner(AppProperties props) {
        var s3 = props.s3();
        var builder = S3Presigner.builder().region(Region.of(s3.region()));
        if (s3.hasEndpoint()) {
            builder.endpointOverride(URI.create(s3.presignEndpoint()))
                   .credentialsProvider(LOCALSTACK_CREDENTIALS)
                   .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        return builder.build();
    }
}
