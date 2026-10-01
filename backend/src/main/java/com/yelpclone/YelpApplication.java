package com.yelpclone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class YelpApplication {
    public static void main(String[] args) {
        SpringApplication.run(YelpApplication.class, args);
    }
}
