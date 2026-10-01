/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.http.client.ClientHttpRequestFactory
 *  org.springframework.http.client.SimpleClientHttpRequestFactory
 *  org.springframework.web.client.RestClient
 */
package com.ridelink.ridemanagement.config;

import com.ridelink.ridemanagement.config.ServiceProperties;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    private final ServiceProperties serviceProperties;

    public RestClientConfig(ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    @Bean
    public RestClient accountServiceRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(this.serviceProperties.getAccount().getConnectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(this.serviceProperties.getAccount().getReadTimeoutMs()));
        return RestClient.builder().baseUrl(this.serviceProperties.getAccount().getUrl()).requestFactory((ClientHttpRequestFactory)requestFactory).build();
    }

    @Bean
    public RestClient driverServiceRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(this.serviceProperties.getDriver().getConnectTimeoutMs()));
        requestFactory.setReadTimeout(Duration.ofMillis(this.serviceProperties.getDriver().getReadTimeoutMs()));
        return RestClient.builder().baseUrl(this.serviceProperties.getDriver().getUrl()).requestFactory((ClientHttpRequestFactory)requestFactory).build();
    }
}

