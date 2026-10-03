package com.ridelink.fare_payment_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Configures the REST client used to call the Ride Management Service.
 */
@Configuration
public class RideClientConfig {

    @Bean
    public RestClient rideServiceRestClient(
            @Value("${services.ride.url:http://localhost:8083}") String baseUrl,
            @Value("${services.ride.connect-timeout-ms:3000}") int connectTimeoutMs,
            @Value("${services.ride.read-timeout-ms:5000}") int readTimeoutMs) {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
