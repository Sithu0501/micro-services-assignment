/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.SpringApplication
 *  org.springframework.boot.autoconfigure.SpringBootApplication
 *  org.springframework.boot.context.properties.EnableConfigurationProperties
 */
package com.ridelink.ridemanagement;

import com.ridelink.ridemanagement.config.ServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(value={ServiceProperties.class})
public class RideManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(RideManagementApplication.class, (String[])args);
    }
}

